package com.alerthub.loaderservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtPermissionValidator {

    private final SecretKey signingKey;

    public JwtPermissionValidator(
            @Value("${jwt.secret-base64}") String secretBase64
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretBase64)
        );
    }

    public void requirePermission(
            String authorizationHeader,
            String requiredPermission
    ) {
        String token = extractToken(authorizationHeader);

        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        List<String> permissions = extractPermissions(claims);

        if (!permissions.contains(requiredPermission)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Missing permission: " + requiredPermission
            );
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header must start with Bearer"
            );
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT token is required"
            );
        }

        return token;
    }

    private List<String> extractPermissions(Claims claims) {
        Object permissionsValue = claims.get("permissions");

        if (!(permissionsValue instanceof List<?> permissions)) {
            return List.of();
        }

        return permissions.stream()
                .map(String::valueOf)
                .toList();
    }
}