package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Despesa;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    Optional<Despesa> findByIdAndEmpresaId(Long id, Long e);

    List<Despesa> findByEmpresaIdAndDataBetweenOrderByDataDesc(Long e, LocalDate i, LocalDate f);

    boolean existsByEmpresaIdAndCategoria(Long e, String categoria);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Despesa d set d.categoria = :novo where d.empresa.id = :e and d.categoria = :antigo")
    int renameCategoria(@Param("e") Long e, @Param("antigo") String antigo, @Param("novo") String novo);
}
