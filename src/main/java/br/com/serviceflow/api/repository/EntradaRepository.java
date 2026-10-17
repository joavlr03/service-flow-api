package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Entrada;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    Optional<Entrada> findByIdAndEmpresaId(Long id, Long e);

    List<Entrada> findByEmpresaIdAndDataBetweenOrderByDataDesc(Long e, LocalDate i, LocalDate f);

    boolean existsByEmpresaIdAndCategoria(Long e, String categoria);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Entrada x set x.categoria = :novo where x.empresa.id = :e and x.categoria = :antigo")
    int renameCategoria(@Param("e") Long e, @Param("antigo") String antigo, @Param("novo") String novo);
}
