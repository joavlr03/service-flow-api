package br.com.serviceflow.api.controller;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.service.ProdutoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/produtos")
@Tag(name = "Produtos e estoque")
public class ProdutoController {
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    private Long e(Jwt j) {
        return j.getClaim("companyId");
    }

    @GetMapping
    public List<ProdutoOut> list(@AuthenticationPrincipal Jwt j) {
        return service.list(e(j));
    }

    @PostMapping
    public ProdutoOut add(@AuthenticationPrincipal Jwt j, @Valid @RequestBody ProdutoIn x) {
        return service.add(e(j), x);
    }

    @PutMapping("/{id}")
    public ProdutoOut update(@AuthenticationPrincipal Jwt j, @PathVariable Long id, @Valid @RequestBody ProdutoIn x) {
        return service.update(e(j), id, x);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
        service.delete(e(j), id);
    }
}
