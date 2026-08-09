package br.com.serviceflow.api.dto.auth;

public record UsuarioResponse(
        Long id,
        Long companyId,
        String name,
        String email,
        String role,
        Boolean passwordChangeRequired) {}
