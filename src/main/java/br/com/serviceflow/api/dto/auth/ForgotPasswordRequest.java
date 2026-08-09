package br.com.serviceflow.api.dto.auth;

import jakarta.validation.constraints.*;

public record ForgotPasswordRequest(@NotBlank @Email String email) {
}
