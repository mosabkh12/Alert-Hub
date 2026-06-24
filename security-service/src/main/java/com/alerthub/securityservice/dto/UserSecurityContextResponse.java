package com.alerthub.securityservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSecurityContextResponse {

    private Long userId;
    private String username;
    private List<String> permissions;
}