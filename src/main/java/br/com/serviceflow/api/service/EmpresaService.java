package br.com.serviceflow.api.service;

import br.com.serviceflow.api.model.Empresa;
import br.com.serviceflow.api.repository.EmpresaRepository;
import br.com.serviceflow.api.dto.domain.DomainDtos.EmpresaIn;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
    private final EmpresaRepository repository;

    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
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
}
