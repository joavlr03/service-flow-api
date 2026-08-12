package br.com.serviceflow.api.controller;

import br.com.serviceflow.api.dto.auth.LoginRequest;
import br.com.serviceflow.api.dto.auth.LoginResponse;
import br.com.serviceflow.api.dto.auth.UsuarioMapper;
import br.com.serviceflow.api.dto.auth.UsuarioResponse;
import br.com.serviceflow.api.service.AuthService;
import br.com.serviceflow.api.service.CredentialTokenService;
import br.com.serviceflow.api.service.SupportRecoveryService;
import br.com.serviceflow.api.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v2/auth")
@Tag(name = "Autenticação")
public class AuthController {
    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;
    private final CredentialTokenService credentialTokenService;
    private final SupportRecoveryService supportRecoveryService;

    public AuthController(AuthService authService, UsuarioMapper usuarioMapper,
                          CredentialTokenService credentialTokenService, SupportRecoveryService supportRecoveryService) {
        this.authService = authService;
        this.usuarioMapper = usuarioMapper;
        this.credentialTokenService = credentialTokenService;
        this.supportRecoveryService = supportRecoveryService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autenticar usuário")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar usuário autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return usuarioMapper.toDto(authService.findAuthenticated(jwt.getSubject()));
    }

    @PostMapping("/refresh") @SecurityRequirements @Operation(summary="Renovar tokens")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request){return credentialTokenService.refresh(request.refreshToken(),usuarioMapper);}

    @PostMapping("/logout") @Operation(summary="Encerrar sessão")
    public MessageResponse logout(@Valid @RequestBody RefreshRequest request){credentialTokenService.logout(request.refreshToken());return new MessageResponse("Sessão encerrada",null);}

    @PostMapping("/esqueci-minha-senha") @SecurityRequirements @Operation(summary="Solicitar recuperação de senha")
    public MessageResponse forgot(@Valid @RequestBody ForgotPasswordRequest request){return supportRecoveryService.request(request);}

    @PostMapping("/redefinir-senha") @SecurityRequirements @Operation(summary="Redefinir senha com token")
    public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest request){credentialTokenService.reset(request);return new MessageResponse("Senha redefinida",null);}

    @PostMapping("/alterar-senha") @Operation(summary="Alterar senha autenticada")
    public MessageResponse change(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ChangePasswordRequest request){credentialTokenService.change(authService.findAuthenticated(jwt.getSubject()),request);return new MessageResponse("Senha alterada; entre novamente",null);}
}
