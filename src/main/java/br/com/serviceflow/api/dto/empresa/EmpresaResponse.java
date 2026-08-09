package br.com.serviceflow.api.dto.empresa;

import java.time.LocalDateTime;

public record EmpresaResponse(
        Long id,
        String name,
        String segment,
        String ownerName,
        String email,
        String plan,
        Boolean active,
        LocalDateTime createdAt) {}
