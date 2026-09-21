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

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 characters."
            );
        }

        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    private SecretKey signingKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String email, String role) {

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public Optional<String> extractEmail(String authorizationHeader) {

        return extractClaims(authorizationHeader)
                .map(ClaimsData::email);
    }

    public Optional<ClaimsData> extractClaims(
            String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            return Optional.empty();
        }

        String token =
                authorizationHeader.substring(7);

        return parseToken(token);
    }

    public Optional<ClaimsData> extractClaimsFromToken(
            String token) {

        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return parseToken(token);
    }

    private Optional<ClaimsData> parseToken(
            String token) {

        try {

            var claims =
                    Jwts.parser()
                            .verifyWith(signingKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            return Optional.of(
                    new ClaimsData(
                            claims.getSubject(),
                            claims.get("role", String.class)
                    )
            );

        } catch (RuntimeException ex) {

            return Optional.empty();
        }
    }

    public record ClaimsData(
            String email,
            String role) {
    }
}