package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Cliente;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEmpresaIdAndNomeContainingIgnoreCaseOrderByNome(Long e, String q);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long e);
}
