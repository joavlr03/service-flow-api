package br.com.serviceflow.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

// E-mail de boas-vindas para a empresa recém-cadastrada. Roda em segundo plano depois do commit:
// o cadastro não espera o envio e não falha se o e-mail falhar.
@Service
public class WelcomeMailService {
    private static final Logger log = LoggerFactory.getLogger(WelcomeMailService.class);
    // O cadastro aceita identificador que não é e-mail (ex.: "joao-central"); esses não recebem.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final JavaMailSender mail;
    private final String from;
    private final boolean mailEnabled;

    public WelcomeMailService(JavaMailSender mail, @Value("${spring.mail.username:}") String from,
            @Value("${app.mail.enabled:false}") boolean mailEnabled) {
        this.mail = mail;
        this.from = from;
        this.mailEnabled = mailEnabled;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmpresaCadastrada(EmpresaCadastradaEvent e) {
        if (!EMAIL.matcher(e.email()).matches()) return;
        if (!mailEnabled || from.isBlank()) {
            log.warn("WELCOME_EMAIL_NOT_CONFIGURED");
            return;
        }
        try {
            MimeMessage message = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, "ServiceFlow");
            helper.setReplyTo(from);
            helper.setTo(e.email());
            helper.setSubject("Bem-vindo ao ServiceFlow, " + e.empresa() + "!");
            helper.setText(texto(e), html(e));
            mail.send(message);
            log.info("WELCOME_EMAIL_SENT");
        } catch (MailException | MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("WELCOME_EMAIL_FAILED type={}", ex.getClass().getSimpleName());
        }
    }

    private static String primeiroNome(String dono) {
        return dono.trim().split("\\s+")[0];
    }

    static String texto(EmpresaCadastradaEvent e) {
        return "Olá, " + primeiroNome(e.dono()) + "!\n\n"
                + "Parabéns por instalar o ServiceFlow. Obrigado pela confiança: estamos aqui para deixar "
                + "o dia a dia da " + e.empresa() + " mais organizado.\n\n"
                + "Próximos passos:\n"
                + "- No primeiro acesso, o app ajuda a configurar seus serviços (ou produtos) e o visual.\n"
                + "- No iPhone, abra o app no Safari e toque em Compartilhar → Adicionar à Tela de Início.\n\n"
                + "Precisa de ajuda? É só responder este e-mail.\n\n"
                + "Equipe ServiceFlow";
    }

    static String html(EmpresaCadastradaEvent e) {
        String nome = HtmlUtils.htmlEscape(primeiroNome(e.dono()));
        String empresa = HtmlUtils.htmlEscape(e.empresa());
        return """
                <!doctype html><html lang="pt-BR"><body style="margin:0;background:#f7f3f0;font-family:Arial,Helvetica,sans-serif;color:#2b1d17">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:24px 12px">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px;background:#ffffff;border-radius:16px;overflow:hidden">
                <tr><td style="background:#4a0c02;padding:28px 28px 24px;color:#ffffff">
                <div style="font-size:24px;font-weight:bold">ServiceFlow</div>
                <div style="font-size:14px;opacity:.8;margin-top:4px">Sua operação de serviços organizada em um só lugar.</div>
                </td></tr>
                <tr><td style="padding:28px">
                <h1 style="font-size:20px;margin:0 0 12px">Olá, %s!</h1>
                <p style="font-size:15px;line-height:1.5;margin:0 0 16px">Parabéns por instalar o <strong>ServiceFlow</strong>. Obrigado pela confiança: estamos aqui para deixar o dia a dia da <strong>%s</strong> mais organizado.</p>
                <p style="font-size:15px;font-weight:bold;margin:0 0 8px">Próximos passos</p>
                <ul style="font-size:14px;line-height:1.6;margin:0 0 16px;padding-left:20px">
                <li>No primeiro acesso, o app ajuda a configurar seus serviços (ou produtos) e o visual.</li>
                <li>No iPhone, abra o app no Safari e toque em <strong>Compartilhar → Adicionar à Tela de Início</strong>.</li>
                </ul>
                <p style="font-size:14px;color:#6b5a52;margin:0">Precisa de ajuda? É só responder este e-mail.</p>
                </td></tr>
                <tr><td style="padding:16px 28px;background:#faf7f5;font-size:12px;color:#8a7a72">Equipe ServiceFlow</td></tr>
                </table></td></tr></table></body></html>
                """.formatted(nome, empresa);
    }
}
