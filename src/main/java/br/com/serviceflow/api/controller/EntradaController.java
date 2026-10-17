package br.com.serviceflow.api.controller;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.service.EntradaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/entradas")
@Tag(name = "Entradas")
public class EntradaController {
    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    private Long e(Jwt j) {
        return j.getClaim("companyId");
    }

    @PostMapping
    public EntradaOut add(@AuthenticationPrincipal Jwt j, @Valid @RequestBody EntradaIn x) {
        return service.add(e(j), x);
    }

    @GetMapping
    public List<EntradaOut> list(@AuthenticationPrincipal Jwt j, @RequestParam LocalDate dataInicial,
            @RequestParam LocalDate dataFinal) {
        return service.list(e(j), dataInicial, dataFinal);
    }

    @GetMapping("/{id}")
    public EntradaOut get(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
        return service.get(e(j), id);
    }

    @PutMapping("/{id}")
    public EntradaOut update(@AuthenticationPrincipal Jwt j, @PathVariable Long id, @Valid @RequestBody EntradaIn x) {
        return service.update(e(j), id, x);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
        service.delete(e(j), id);
    }
}
