package br.com.serviceflow.api.controller;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/categorias")
@Tag(name = "Categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    private Long e(Jwt j) {
        return j.getClaim("companyId");
    }

    @GetMapping
    public List<CategoriaOut> list(@AuthenticationPrincipal Jwt j, @RequestParam(defaultValue = "DESPESA") String tipo) {
        return service.list(e(j), tipo);
    }

    @PostMapping
    public CategoriaOut add(@AuthenticationPrincipal Jwt j, @Valid @RequestBody CategoriaIn x) {
        return service.add(e(j), x);
    }

    @PutMapping("/{id}")
    public CategoriaOut update(@AuthenticationPrincipal Jwt j, @PathVariable Long id, @Valid @RequestBody CategoriaIn x) {
        return service.update(e(j), id, x);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
        service.delete(e(j), id);
    }
}
