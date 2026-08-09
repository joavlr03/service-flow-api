package br.com.serviceflow.api.dto.empresa;

import br.com.serviceflow.api.model.Empresa;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {
    public EmpresaResponse toDto(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(), empresa.getNome(), empresa.getSegmento(),
                empresa.getNomeProprietario(), empresa.getEmail(), empresa.getPlano(),
                empresa.getAtivo(), empresa.getCriadoEm());
    }
}
