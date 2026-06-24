package com.alerthub.securityservice.controller;

import com.alerthub.securityservice.dto.LoginRequest;
import com.alerthub.securityservice.dto.LoginResponse;
import com.alerthub.securityservice.service.SecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alerthub.securityservice.dto.TokenValidationResponse;
import org.springframework.http.HttpHeaders;
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                securityService.login(request)
        );
    }
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
        return ResponseEntity.ok(
                securityService.validateToken(authorizationHeader)
        );
    }

}