package com.aqConnecta.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public String generateToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret.getBytes())
            .compact();
    }

    public String generateRefreshToken(UUID id, String email) {
        return this.generateRefreshToken(id, email, this.refreshExpiration);
    }

    public String generateRefreshToken(UUID id, String email, long expirationInMillis) {
        var claims = new HashMap<String, Object>();
        claims.put("email", email);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(id.toString())
            .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
            .signWith(SignatureAlgorithm.HS512, secret.getBytes())
            .setId(UUID.randomUUID().toString())
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
            return Optional.of(Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody());
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
}
