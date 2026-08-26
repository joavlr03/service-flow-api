package br.com.serviceflow.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 180) String email,
        @NotBlank @Size(max = 72) String password) {}
