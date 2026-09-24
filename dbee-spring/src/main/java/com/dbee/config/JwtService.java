package com.dbee.config;

import com.dbee.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    private SecretKey key() {
        if (properties.jwtSecret() == null || properties.jwtSecret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long.");
        }
        return Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + properties.jwtExpirationMs()))
                .signWith(key())
                .compact();
    }

    public Long parseUserId(String token) {
        return Long.valueOf(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject());
    }
}
