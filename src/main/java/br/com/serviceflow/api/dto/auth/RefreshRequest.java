package br.com.serviceflow.api.dto.auth; import jakarta.validation.constraints.NotBlank; public record RefreshRequest(@NotBlank String refreshToken){}
