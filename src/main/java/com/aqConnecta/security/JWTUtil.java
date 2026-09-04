package com.aqConnecta.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Component
public class JWTUtil {

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret não configurado. Defina a variável de ambiente JWT_SECRET com pelo menos 32 caracteres."
            );
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS512 exige no mínimo 64 bytes (512 bits). Se o segredo for menor,
        // derivamos uma chave de 64 bytes via SHA-512 — assim não quebramos
        // deploys existentes, mas garantimos que a chave usada é forte o suficiente.
        if (bytes.length < 64) {
            try {
                bytes = MessageDigest.getInstance("SHA-512").digest(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-512 indisponível na JVM", e);
            }
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(UUID id, String email) {
        return this.generateRefreshToken(id, email, this.refreshExpiration);
    }

    public String generateRefreshToken(UUID id, String email, long expirationInMillis) {
        var claims = new HashMap<String, Object>();
        claims.put("email", email);

        return Jwts.builder()
                .claims(claims)
                .subject(id.toString())
                .id(UUID.randomUUID().toString())
                .expiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(signingKey)
                .compact();
    }

    public boolean refreshTokenValido(Claims claims) {
        return claims.getSubject() != null && claims.get("email") != null;
    }

    public boolean tokenValido(Claims claims) {
        return claims.getSubject() != null;
    }

    public RefreshTokenClaims montarRefreshTokenClaims(Claims claims) {
        return new RefreshTokenClaims(UUID.fromString(claims.getSubject()), claims.get("email").toString());
    }

    public Optional<Claims> getTokenValidatedClaims(String token) {
        try {
            return Optional.of(
                    Jwts.parser()
                            .verifyWith(signingKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
