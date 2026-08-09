package br.com.serviceflow.api.dto.auth;

import jakarta.validation.constraints.*;

public record SupportResetRequest(@NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String temporaryPassword) {
}
