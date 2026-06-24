package com.alerthub.securityservice.service;

import com.alerthub.securityservice.dto.LoginRequest;
import com.alerthub.securityservice.dto.LoginResponse;
import com.alerthub.securityservice.dto.TokenValidationResponse;
import com.alerthub.securityservice.dto.UserResponse;
import com.alerthub.securityservice.dto.UserSecurityContextResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SecurityService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final String userServiceUrl;

    public SecurityService(
            RestTemplate restTemplate,
            JwtService jwtService,
            @Value("${services.user.url}") String userServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.userServiceUrl = userServiceUrl;
    }

    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        UserResponse user;

        try {
            ResponseEntity<UserResponse> response =
                    restTemplate.postForEntity(
                            userServiceUrl + "/api/users/authenticate",
                            request,
                            UserResponse.class
                    );

            user = response.getBody();

        } catch (HttpClientErrorException exception) {
            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User authentication failed"
            );
        }

        UserSecurityContextResponse securityContext =
                restTemplate.exchange(
                        userServiceUrl
                                + "/api/users/"
                                + user.getId()
                                + "/security-context",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        UserSecurityContextResponse.class
                ).getBody();

        List<String> permissions =
                securityContext == null
                        || securityContext.getPermissions() == null
                        ? List.of()
                        : securityContext.getPermissions();

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                permissions
        );

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                permissions
        );
    }

    public TokenValidationResponse validateToken(
            String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);

        try {
            Claims claims = jwtService.parseToken(token);

            Object userIdValue = claims.get("userId");

            if (!(userIdValue instanceof Number userIdNumber)) {
                throw new IllegalArgumentException(
                        "Token does not contain a valid user ID"
                );
            }

            List<String> permissions = extractPermissions(claims);

            return new TokenValidationResponse(
                    true,
                    userIdNumber.longValue(),
                    claims.getSubject(),
                    permissions
            );

        } catch (JwtException exception) {
            throw new IllegalArgumentException(
                    "Invalid or expired token"
            );
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "Authorization header must start with Bearer"
            );
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isBlank()) {
            throw new IllegalArgumentException(
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

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Login request is required"
            );
        }

        if (request.getUsername() == null
                || request.getUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required"
            );
        }
    }
}