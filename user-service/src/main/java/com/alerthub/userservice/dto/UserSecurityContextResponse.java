package com.alerthub.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserSecurityContextResponse {

    private Long userId;
    private String username;
    private List<String> permissions;
}