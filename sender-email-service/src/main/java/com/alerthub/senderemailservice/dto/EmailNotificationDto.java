package com.alerthub.senderemailservice.dto;

import lombok.Data;

@Data
public class EmailNotificationDto {

    private Long actionId;
    private String recipient;
    private String message;
    private String createdAt;
}