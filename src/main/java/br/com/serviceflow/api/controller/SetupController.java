package br.com.serviceflow.api.controller;

import br.com.serviceflow.api.dto.setup.SetupRequest;
import br.com.serviceflow.api.dto.setup.SetupResponse;
import br.com.serviceflow.api.service.SetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/setup")
@Tag(name = "Configuração inicial")
public class SetupController {
    private final SetupService service;
    public SetupController(SetupService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Cadastrar empresa e proprietário na primeira instalação")
    public SetupResponse setup(@Valid @RequestBody SetupRequest request) {
        return service.setup(request);
    }
}
