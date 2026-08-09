package br.com.serviceflow.api.dto.setup;

public record SetupResponse(
        Long companyId,
        Long userId,
        String ownerName,
        String email,
        String role,
        String status) {}
