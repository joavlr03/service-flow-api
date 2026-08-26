package br.com.serviceflow.api.config;

import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.model.Usuario;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.test-data.enabled", havingValue = "true")
public class TestDataConfig {
    @Bean
    CommandLineRunner testAdmin(EmpresaRepository empresas, UsuarioRepository usuarios,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarios.count() > 0) return;
            LocalDateTime now = LocalDateTime.now();
            Empresa empresa = new Empresa();
            empresa.setNome("Empresa de Teste");
            empresa.setSegmento("Estética Automotiva");
            empresa.setNomeProprietario("Administrador de Teste");
            empresa.setEmail("admin@brilhototal.com.br");
            empresa.setPlano("Operacional + Financeiro");
            empresa.setAtivo(true);
            empresa.setCriadoEm(now);
            empresa = empresas.save(empresa);

            Usuario usuario = new Usuario();
            usuario.setEmpresa(empresa);
            usuario.setNome("Administrador de Teste");
            usuario.setEmail("admin@brilhototal.com.br");
            usuario.setSenhaHash(passwordEncoder.encode("AdminTest@123"));
            usuario.setPerfil("ADMIN");
            usuario.setAtivo(true);
            usuario.setTrocaSenhaObrigatoria(false);
            usuario.setCriadoEm(now);
            usuarios.save(usuario);
        };
    }
}
