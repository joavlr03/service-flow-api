package br.com.serviceflow.api.repository;

import br.com.serviceflow.api.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {}
