package br.com.serviceflow.api.service;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.model.Categoria;
import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.repository.CategoriaRepository;
import br.com.serviceflow.api.repository.DespesaRepository;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.EntradaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoriaService {
    // Usadas no cadastro de empresa nova; empresas antigas receberam a mesma lista na V8.
    public static final List<String> PADRAO_DESPESA = List.of("Produtos", "Água e Energia", "Aluguel",
            "Funcionários", "Manutenção", "Impostos", "Fornecedores", "Outros");
    public static final List<String> PADRAO_ENTRADA = List.of("Venda de produto", "Serviço avulso", "Comissão", "Outros");

    private final CategoriaRepository repository;
    private final DespesaRepository despesas;
    private final EntradaRepository entradas;
    private final EmpresaRepository empresas;

    public CategoriaService(CategoriaRepository repository, DespesaRepository despesas, EntradaRepository entradas,
            EmpresaRepository empresas) {
        this.repository = repository;
        this.despesas = despesas;
        this.entradas = entradas;
        this.empresas = empresas;
    }

    @Transactional(readOnly = true)
    public List<CategoriaOut> list(Long e, String tipo) {
        return repository.findByEmpresaIdAndTipoOrderByNome(e, tipo).stream().map(this::out).toList();
    }

    public CategoriaOut add(Long e, CategoriaIn x) {
        String nome = x.name().trim();
        checkUnique(e, x.type(), nome, null);
        Empresa empresa = empresas.findById(e).orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
        Categoria c = new Categoria(empresa, x.type(), nome);
        if (x.active() != null) c.setAtivo(x.active());
        return out(repository.save(c));
    }

    public CategoriaOut update(Long e, Long id, CategoriaIn x) {
        Categoria c = find(e, id);
        String nome = x.name().trim();
        checkUnique(e, c.getTipo(), nome, id);
        String antigo = c.getNome();
        c.setNome(nome);
        if (x.active() != null) c.setAtivo(x.active());
        // Renomear vale também para o histórico, senão o Financeiro e o Excel mostram dois nomes.
        // Fica por último: a query grava a categoria antes e limpa o contexto depois.
        if (!antigo.equals(nome)) {
            if (c.getTipo().equals("DESPESA")) despesas.renameCategoria(e, antigo, nome);
            else entradas.renameCategoria(e, antigo, nome);
        }
        return out(c);
    }

    public void delete(Long e, Long id) {
        Categoria c = find(e, id);
        boolean emUso = c.getTipo().equals("DESPESA")
                ? despesas.existsByEmpresaIdAndCategoria(e, c.getNome())
                : entradas.existsByEmpresaIdAndCategoria(e, c.getNome());
        if (emUso)
            throw new IllegalArgumentException("Categoria já usada em lançamentos; desative-a em vez de excluir");
        repository.delete(c);
    }

    private void checkUnique(Long e, String tipo, String nome, Long self) {
        repository.findByEmpresaIdAndTipoAndNomeIgnoreCase(e, tipo, nome)
                .filter(c -> !c.getId().equals(self))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Já existe uma categoria com esse nome");
                });
    }

    private Categoria find(Long e, Long id) {
        return repository.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }

    private CategoriaOut out(Categoria c) {
        return new CategoriaOut(c.getId(), c.getNome(), c.getTipo(), c.getAtivo());
    }
}
