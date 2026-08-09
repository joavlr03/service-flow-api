package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.OrdemServico;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByIdAndEmpresaId(Long id, Long e);

    List<OrdemServico> findByEmpresaIdAndDataBetweenOrderByDataAscHorarioAsc(Long e, LocalDate i, LocalDate f);

    List<OrdemServico> findByEmpresaIdAndClienteIdOrderByDataDescHorarioDesc(Long e, Long c);

    long countByEmpresaId(Long e);

    boolean existsByEmpresaIdAndClienteId(Long e, Long c);
    boolean existsByEmpresaIdAndVeiculoId(Long e, Long v);
    boolean existsByEmpresaIdAndTipoServicoId(Long e, Long s);
}
