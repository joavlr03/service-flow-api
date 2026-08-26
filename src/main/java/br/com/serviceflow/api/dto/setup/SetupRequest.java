package br.com.serviceflow.api.dto.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetupRequest(
        @NotBlank String companyName,
        @NotBlank String segment,
        @NotBlank String ownerName,
        @NotBlank @Size(min = 3, max = 180) String email,
        @NotBlank @Size(min = 12, max = 72) String password) {}
