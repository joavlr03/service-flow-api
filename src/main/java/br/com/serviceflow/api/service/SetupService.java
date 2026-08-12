package br.com.serviceflow.api.service;

import br.com.serviceflow.api.dto.setup.SetupRequest;
import br.com.serviceflow.api.dto.setup.SetupResponse;
import br.com.serviceflow.api.exception.SetupAlreadyCompletedException;
import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.model.Usuario;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.UsuarioRepository;
import br.com.serviceflow.api.repository.TipoServicoRepository;
import br.com.serviceflow.api.model.TipoServico;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetupService {
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipoServicoRepository tipoServicoRepository;
    private final String setupKey;

    public SetupService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder, TipoServicoRepository tipoServicoRepository,
                        @Value("${app.setup.key}") String setupKey) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tipoServicoRepository = tipoServicoRepository;
        this.setupKey = setupKey;
    }

    @Transactional
    public synchronized SetupResponse setup(String suppliedKey, SetupRequest request) {
        if (setupKey.isBlank() || suppliedKey == null ||
                !MessageDigest.isEqual(setupKey.getBytes(StandardCharsets.UTF_8), suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new BadCredentialsException("Credencial de setup inválida");
        }
        if (usuarioRepository.count() > 0) throw new SetupAlreadyCompletedException();

        LocalDateTime now = LocalDateTime.now();
        Empresa empresa = new Empresa();
        empresa.setNome(request.companyName().trim());
        empresa.setSegmento(request.segment().trim());
        empresa.setNomeProprietario(request.ownerName().trim());
        empresa.setEmail(request.email().trim().toLowerCase());
        empresa.setPlano(request.plan().trim());
        empresa.setAtivo(true);
        empresa.setCriadoEm(now);
        empresa = empresaRepository.save(empresa);

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome(request.ownerName().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(request.password()));
        usuario.setPerfil("ADMIN");
        usuario.setAtivo(true);
        usuario.setTrocaSenhaObrigatoria(false);
        usuario.setCriadoEm(now);
        usuario = usuarioRepository.save(usuario);

        criarServico(empresa, "Lavagem Simples", "45.00", 40, now);
        criarServico(empresa, "Lavagem Completa", "80.00", 70, now);
        criarServico(empresa, "Polimento", "220.00", 180, now);
        criarServico(empresa, "Higienização Interna", "160.00", 120, now);
        criarServico(empresa, "Cera Premium", "120.00", 90, now);

        return new SetupResponse(empresa.getId(), usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getPerfil(), "ACTIVE");
    }

    private void criarServico(Empresa empresa, String nome, String preco, int duracao, LocalDateTime now) {
        TipoServico servico = new TipoServico(); servico.setEmpresa(empresa); servico.setNome(nome);
        servico.setPrecoPadrao(new BigDecimal(preco)); servico.setDuracaoMinutos(duracao);
        servico.setAtivo(true); servico.setCriadoEm(now); tipoServicoRepository.save(servico);
    }
}
