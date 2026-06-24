package com.alerthub.securityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String username;
    private List<String> permissions;
}