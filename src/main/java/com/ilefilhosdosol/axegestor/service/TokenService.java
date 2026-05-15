package com.ilefilhosdosol.axegestor.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ilefilhosdosol.axegestor.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final String secret;
    private final long expirationHours;

    public TokenService(
            @Value("${api.security.token.secret}") String secret,
            @Value("${api.security.token.expiration-hours}") long expirationHours
    ) {
        this.secret = secret;
        this.expirationHours = expirationHours;
    }

    public String gerarToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer("axegestor")
                .withSubject(usuario.getEmail())
                .withClaim("perfil", usuario.getPerfil().name())
                .withExpiresAt(gerarDataExpiracao())
                .sign(algorithm);
    }

    public String validarToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.require(algorithm)
                .withIssuer("axegestor")
                .build()
                .verify(token)
                .getSubject();
    }

    private Instant gerarDataExpiracao() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }
}
