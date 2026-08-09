package br.com.serviceflow.api.dto.setup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetupRequest(
        @NotBlank String companyName,
        @NotBlank String segment,
        @NotBlank String ownerName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String plan) {}
