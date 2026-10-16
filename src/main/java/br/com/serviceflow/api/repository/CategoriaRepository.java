package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Categoria;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByEmpresaIdAndTipoOrderByNome(Long e, String tipo);

    Optional<Categoria> findByIdAndEmpresaId(Long id, Long e);

    Optional<Categoria> findByEmpresaIdAndTipoAndNomeIgnoreCase(Long e, String tipo, String nome);
}
