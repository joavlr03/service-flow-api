package br.com.serviceflow.api.service;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;

import br.com.serviceflow.api.model.Entrada;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.repository.EntradaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntradaService {
    private final EntradaRepository repository;
    private final EmpresaRepository empresas;

    public EntradaService(EntradaRepository repository, EmpresaRepository empresas) {
        this.repository = repository;
        this.empresas = empresas;
    }

    public EntradaOut add(Long e, EntradaIn x) {
        Entrada n = new Entrada();
        n.setEmpresa(empresas.findById(e).orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada")));
        n.setCriadoEm(LocalDateTime.now());
        return out(repository.save(fill(n, x)));
    }

    @Transactional(readOnly = true)
    public List<EntradaOut> list(Long e, LocalDate a, LocalDate b) {
        if (a.isAfter(b) || ChronoUnit.DAYS.between(a, b) > 366)
            throw new IllegalArgumentException("Período inválido; use no máximo 366 dias");
        return repository.findByEmpresaIdAndDataBetweenOrderByDataDesc(e, a, b).stream().map(this::out).toList();
    }

    @Transactional(readOnly = true)
    public EntradaOut get(Long e, Long id) {
        return out(find(e, id));
    }

    public EntradaOut update(Long e, Long id, EntradaIn x) {
        return out(fill(find(e, id), x));
    }

    public void delete(Long e, Long id) {
        repository.delete(find(e, id));
    }

    private Entrada fill(Entrada n, EntradaIn x) {
        n.setDescricao(x.description().trim());
        n.setCategoria(x.category().trim());
        n.setValor(x.amount());
        n.setData(x.date());
        n.setObservacoes(x.notes());
        return n;
    }

    private Entrada find(Long e, Long id) {
        return repository.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Entrada não encontrada"));
    }

    private EntradaOut out(Entrada n) {
        return new EntradaOut(n.getId(), n.getDescricao(), n.getCategoria(), n.getValor(), n.getData(),
                n.getObservacoes());
    }
}
