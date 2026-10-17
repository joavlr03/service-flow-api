package br.com.serviceflow.api.service;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.model.Produto;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProdutoService {
    private final ProdutoRepository repository;
    private final EmpresaRepository empresas;

    public ProdutoService(ProdutoRepository repository, EmpresaRepository empresas) {
        this.repository = repository;
        this.empresas = empresas;
    }

    @Transactional(readOnly = true)
    public List<ProdutoOut> list(Long e) {
        return repository.findByEmpresaIdOrderByNome(e).stream().map(ProdutoService::out).toList();
    }

    public ProdutoOut add(Long e, ProdutoIn x) {
        String nome = x.name().trim();
        checkUnique(e, nome, null);
        Produto p = new Produto();
        p.setEmpresa(empresas.findById(e).orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada")));
        p.setCriadoEm(LocalDateTime.now());
        return out(fill(p, nome, x));
    }

    public ProdutoOut update(Long e, Long id, ProdutoIn x) {
        String nome = x.name().trim();
        checkUnique(e, nome, id);
        return out(fill(find(e, id), nome, x));
    }

    public void delete(Long e, Long id) {
        Produto p = find(e, id);
        if (repository.isUsedInSales(id))
            throw new IllegalArgumentException("Produto já vendido; desative-o em vez de excluir");
        repository.delete(p);
    }

    private Produto fill(Produto p, String nome, ProdutoIn x) {
        p.setNome(nome);
        p.setPreco(x.price());
        // Estoque informado é o saldo atual (ajuste manual: contagem, reposição).
        p.setEstoque(x.stock());
        if (x.active() != null) p.setAtivo(x.active());
        return repository.save(p);
    }

    private void checkUnique(Long e, String nome, Long self) {
        repository.findByEmpresaIdAndNomeIgnoreCase(e, nome)
                .filter(p -> !p.getId().equals(self))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Já existe um produto com esse nome");
                });
    }

    private Produto find(Long e, Long id) {
        return repository.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    static ProdutoOut out(Produto p) {
        return new ProdutoOut(p.getId(), p.getNome(), p.getPreco(), p.getEstoque(), p.getAtivo());
    }
}
