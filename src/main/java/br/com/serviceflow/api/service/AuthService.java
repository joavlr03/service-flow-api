package br.com.serviceflow.api.service;

import br.com.serviceflow.api.dto.auth.LoginRequest;
import br.com.serviceflow.api.dto.auth.LoginResponse;
import br.com.serviceflow.api.dto.auth.UsuarioMapper;
import br.com.serviceflow.api.model.Usuario;
import br.com.serviceflow.api.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UsuarioMapper usuarioMapper;
    private final CredentialTokenService credentialTokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService, UsuarioMapper usuarioMapper,
                       CredentialTokenService credentialTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.usuarioMapper = usuarioMapper;
        this.credentialTokenService = credentialTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        if (!passwordEncoder.matches(request.password(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }
        usuario.setUltimoLoginEm(LocalDateTime.now());
        return new LoginResponse(
                tokenService.createAccessToken(usuario), credentialTokenService.issue(usuario), "Bearer",
                tokenService.expiresInSeconds(), usuarioMapper.toDto(usuario));
    }

    @Transactional(readOnly = true)
    public Usuario findAuthenticated(String userId) {
        try {
            return usuarioRepository.findById(Long.valueOf(userId))
                    .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                    .orElseThrow(() -> new BadCredentialsException("Usuário inválido"));
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("Token inválido");
        }
    }
}
