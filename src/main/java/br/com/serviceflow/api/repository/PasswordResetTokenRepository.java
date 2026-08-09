package br.com.serviceflow.api.repository; import br.com.serviceflow.api.model.PasswordResetToken; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long>{Optional<PasswordResetToken> findByTokenHash(String hash);}
