package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Produto;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByEmpresaIdOrderByNome(Long e);

    Optional<Produto> findByIdAndEmpresaId(Long id, Long e);

    Optional<Produto> findByEmpresaIdAndNomeIgnoreCase(Long e, String nome);

    @Query("select count(i) > 0 from ItemOrdem i where i.produto.id = :id")
    boolean isUsedInSales(@Param("id") Long id);
}
