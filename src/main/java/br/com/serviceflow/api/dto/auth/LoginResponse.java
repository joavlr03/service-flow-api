package br.com.serviceflow.api.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse user) {}
