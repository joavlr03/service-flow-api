package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Despesa;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    Optional<Despesa> findByIdAndEmpresaId(Long id, Long e);

    List<Despesa> findByEmpresaIdAndDataBetweenOrderByDataDesc(Long e, LocalDate i, LocalDate f);
}
