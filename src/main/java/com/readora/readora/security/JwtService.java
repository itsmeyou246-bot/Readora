package com.readora.readora.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String email, String role) {

        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        long expirationTime = 1000 * 60 * 60;

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public Optional<String> extractEmail(String authorizationHeader) {
        return extractClaims(authorizationHeader).map(ClaimsData::email);
    }

    public Optional<ClaimsData> extractClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorizationHeader.substring(7))
                    .getPayload();
            return Optional.of(new ClaimsData(
                    claims.getSubject(),
                    claims.get("role", String.class)
            ));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public record ClaimsData(String email, String role) {
    }
}