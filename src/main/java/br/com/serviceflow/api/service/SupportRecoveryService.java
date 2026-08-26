package br.com.serviceflow.api.service;

import br.com.serviceflow.api.dto.auth.ForgotPasswordRequest;
import br.com.serviceflow.api.dto.auth.MessageResponse;
import br.com.serviceflow.api.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SupportRecoveryService {
    private static final Logger log = LoggerFactory.getLogger(SupportRecoveryService.class);
    private static final String RESPONSE =
            "Se o identificador estiver cadastrado, a solicitação será encaminhada ao suporte";
    private final UsuarioRepository users;
    private final JavaMailSender mail;
    private final String supportEmail;
    private final String from;
    private final boolean mailEnabled;

    public SupportRecoveryService(UsuarioRepository users, JavaMailSender mail,
            @Value("${app.support.email:qorelab.contato@gmail.com}") String supportEmail,
            @Value("${spring.mail.username:}") String from,
            @Value("${app.mail.enabled:false}") boolean mailEnabled) {
        this.users = users;
        this.mail = mail;
        this.supportEmail = supportEmail;
        this.from = from;
        this.mailEnabled = mailEnabled;
    }

    public MessageResponse request(ForgotPasswordRequest request) {
        users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
            if (!mailEnabled || supportEmail.isBlank() || from.isBlank()) {
                log.warn("RECOVERY_EMAIL_NOT_CONFIGURED userId={}", user.getId());
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(supportEmail);
            message.setSubject("ServiceFlow - solicitação de redefinição de senha");
            message.setText("Uma solicitação de recuperação de acesso foi recebida.\n\n"
                    + "Usuário: " + user.getNome() + "\n"
                    + "Identificador: " + user.getEmail() + "\n"
                    + "Data: " + LocalDateTime.now() + "\n\n"
                    + "Nenhuma senha foi alterada. Confirme a identidade do solicitante antes de prestar suporte.");
            try {
                mail.send(message);
                log.info("RECOVERY_EMAIL_SENT userId={}", user.getId());
            } catch (MailException exception) {
                log.error("RECOVERY_EMAIL_FAILED userId={} type={}", user.getId(),
                        exception.getClass().getSimpleName());
            }
        });
        return new MessageResponse(RESPONSE, null);
    }
}
