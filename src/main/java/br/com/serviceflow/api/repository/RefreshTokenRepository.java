package br.com.serviceflow.api.repository; import br.com.serviceflow.api.model.RefreshToken; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>{Optional<RefreshToken> findByTokenHash(String hash); java.util.List<RefreshToken> findByUsuarioIdAndRevogadoFalse(Long userId);}
