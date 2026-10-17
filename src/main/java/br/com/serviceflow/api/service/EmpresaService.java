package br.com.serviceflow.api.service;

import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.dto.domain.DomainDtos.EmpresaIn;
import br.com.serviceflow.api.dto.domain.DomainDtos.OnboardingIn;
import br.com.serviceflow.api.exception.OnboardingAlreadyCompletedException;
import br.com.serviceflow.api.model.Produto;
import br.com.serviceflow.api.model.TipoServico;
import br.com.serviceflow.api.repository.ProdutoRepository;
import br.com.serviceflow.api.repository.TipoServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {
    private final EmpresaRepository repository;
    private final TipoServicoRepository servicos;
    private final ProdutoRepository produtos;

    public EmpresaService(EmpresaRepository repository, TipoServicoRepository servicos, ProdutoRepository produtos) {
        this.repository = repository;
        this.servicos = servicos;
        this.produtos = produtos;
    }

    // Grava tudo do assistente de primeiro acesso de uma vez; nada fica salvo se algo falhar.
    @Transactional
    public Empresa onboarding(Long id, OnboardingIn x) {
        Empresa empresa = findById(id);
        if (Boolean.TRUE.equals(empresa.getOnboardingConcluido())) throw new OnboardingAlreadyCompletedException();
        boolean vendas = x.usageMode().equals("VENDAS");
        if (vendas ? x.products() == null || x.products().isEmpty() : x.services() == null || x.services().isEmpty())
            throw new IllegalArgumentException(vendas ? "Cadastre ao menos um produto" : "Cadastre ao menos um serviço");
        LocalDateTime now = LocalDateTime.now();
        empresa.setNome(x.name().trim());
        empresa.setSegmento(x.segment().trim());
        empresa.setModoUso(x.usageMode());
        empresa.setUsaVeiculo(!vendas && x.usesVehicle()); // venda não tem veículo (spec 006)
        empresa.setUsaDuracao(x.usesDuration());
        if (x.themeColor() != null) empresa.setCorPrimaria(x.themeColor().toUpperCase());
        empresa.setIcone(x.appIcon());
        if (vendas) {
            x.products().forEach(p -> {
                Produto n = new Produto();
                n.setEmpresa(empresa);
                n.setNome(p.name().trim());
                n.setPreco(p.price());
                n.setEstoque(p.stock());
                n.setCriadoEm(now);
                produtos.save(n);
            });
        } else {
            x.services().forEach(s -> {
                TipoServico n = new TipoServico();
                n.setEmpresa(empresa);
                n.setNome(s.name().trim());
                n.setPrecoPadrao(s.price());
                // Sem controle de duração a coluna continua obrigatória: guarda 60 min.
                n.setDuracaoMinutos(s.durationMin() == null ? 60 : s.durationMin());
                n.setAtivo(true);
                n.setCriadoEm(now);
                servicos.save(n);
            });
        }
        empresa.setOnboardingConcluido(true);
        return repository.save(empresa);
    }

    public Empresa findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
    }

    public Empresa update(Long id, EmpresaIn request) {
        Empresa empresa = findById(id);
        empresa.setNome(request.name()); empresa.setSegmento(request.segment());
        empresa.setNomeProprietario(request.ownerName()); empresa.setEmail(request.email());
        empresa.setPlano(request.plan());
        return repository.save(empresa);
    }

    public Empresa updateTema(Long id, String color) {
        Empresa empresa = findById(id);
        empresa.setCorPrimaria(color.toUpperCase());
        return repository.save(empresa);
    }
}
