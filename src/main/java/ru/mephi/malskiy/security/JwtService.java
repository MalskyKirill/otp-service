package ru.mephi.malskiy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ru.mephi.malskiy.config.AppConfig;
import ru.mephi.malskiy.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JwtService {
    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtService(AppConfig config) {
        this.secretKey = Keys.hmacShaKeyFor(config.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = config.getJwtExpirationMinutes();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("login", user.getLogin())
            .claim("role", user.getRole())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

}
