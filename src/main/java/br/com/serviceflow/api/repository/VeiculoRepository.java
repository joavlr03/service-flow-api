package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Veiculo;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findByEmpresaIdAndClienteIdOrderByMarca(Long e, Long c);

    Optional<Veiculo> findByIdAndEmpresaId(Long id, Long e);

    boolean existsByEmpresaIdAndClienteId(Long e, Long c);
}
