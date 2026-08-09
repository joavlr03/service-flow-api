package br.com.serviceflow.api.repository; import br.com.serviceflow.api.model.TipoServico; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface TipoServicoRepository extends JpaRepository<TipoServico,Long>{ List<TipoServico> findByEmpresaIdOrderByNome(Long e); Optional<TipoServico> findByIdAndEmpresaId(Long id,Long e); }
