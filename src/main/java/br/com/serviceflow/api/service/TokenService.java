package br.com.serviceflow.api.service;

import br.com.serviceflow.api.model.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenDuration;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.security.access-token-minutes}") long minutes) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenDuration = Duration.ofMinutes(minutes);
    }

    public String createAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("serviceflow-api")
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenDuration))
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("companyId", usuario.getEmpresa().getId())
                .claim("role", usuario.getPerfil())
                .claim("passwordChangeRequired", Boolean.TRUE.equals(usuario.getTrocaSenhaObrigatoria()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return accessTokenDuration.toSeconds();
    }
}
