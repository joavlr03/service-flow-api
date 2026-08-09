package br.com.serviceflow.api.config;

import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.model.Usuario;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapDataConfig {
    @Bean
    CommandLineRunner bootstrapAdmin(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:admin@lavarapidobrizzi.com.br}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:admin123}") String password) {
        return args -> {
            if (usuarioRepository.existsByEmailIgnoreCase(email)) return;

            Empresa empresa = new Empresa();
            empresa.setNome("Lava-Rápido Brizzi");
            empresa.setSegmento("Estética Automotiva");
            empresa.setNomeProprietario("Carlos Brizzi");
            empresa.setEmail(email);
            empresa.setPlano("Operacional + Financeiro");
            empresa.setAtivo(true);
            empresa.setCriadoEm(LocalDateTime.now());
            empresa = empresaRepository.save(empresa);

            Usuario usuario = new Usuario();
            usuario.setEmpresa(empresa);
            usuario.setNome("Carlos Brizzi");
            usuario.setEmail(email.toLowerCase());
            usuario.setSenhaHash(passwordEncoder.encode(password));
            usuario.setPerfil("ADMIN");
            usuario.setAtivo(true);
            usuario.setTrocaSenhaObrigatoria(false);
            usuario.setCriadoEm(LocalDateTime.now());
            usuarioRepository.save(usuario);
        };
    }
}
