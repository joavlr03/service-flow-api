package br.com.serviceflow.api.controller;

import br.com.serviceflow.api.dto.empresa.EmpresaMapper;
import br.com.serviceflow.api.dto.empresa.EmpresaResponse;
import br.com.serviceflow.api.service.EmpresaService;
import br.com.serviceflow.api.dto.domain.DomainDtos.EmpresaIn;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/empresa")
@Tag(name = "Empresa")
public class EmpresaController {
    private final EmpresaService service;
    private final EmpresaMapper mapper;

    public EmpresaController(EmpresaService service, EmpresaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Consultar a empresa do usuário autenticado")
    public EmpresaResponse findCurrent(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toDto(service.findById(jwt.getClaim("companyId")));
    }

    @PutMapping
    @Operation(summary = "Atualizar a empresa do usuário autenticado")
    public EmpresaResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EmpresaIn request) {
        return mapper.toDto(service.update(jwt.getClaim("companyId"), request));
    }
}
