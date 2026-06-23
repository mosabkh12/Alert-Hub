package com.alerthub.sendersmsservice.dto;

import lombok.Data;

@Data
public class SmsNotificationDto {

    private Long actionId;
    private String phoneNumber;
    private String message;
    private String createdAt;
}