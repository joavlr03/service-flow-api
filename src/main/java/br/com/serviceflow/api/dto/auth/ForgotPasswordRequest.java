package br.com.serviceflow.api.dto.auth;

import jakarta.validation.constraints.*;

public record ForgotPasswordRequest(@NotBlank @Size(min = 3, max = 180) String email) {
}
