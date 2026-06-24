package com.alerthub.securityservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret-base64}") String secretBase64,
            @Value("${jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretBase64)
        );
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(
            Long userId,
            String username,
            List<String> permissions
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("permissions", permissions)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        expirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                )
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}