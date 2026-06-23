package com.alerthub.sendersmsservice.kafka;

import com.alerthub.sendersmsservice.dto.SmsNotificationDto;
import com.alerthub.sendersmsservice.dto.SmsRequest;
import com.alerthub.sendersmsservice.service.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(SmsNotificationConsumer.class);

    private final ObjectMapper objectMapper;
    private final SmsService smsService;

    public SmsNotificationConsumer(
            ObjectMapper objectMapper,
            SmsService smsService
    ) {
        this.objectMapper = objectMapper;
        this.smsService = smsService;
    }

    @KafkaListener(
            topics = "sms-notifications",
            groupId = "sender-sms-service"
    )
    public void consumeSmsNotification(String payload) throws Exception {
        SmsNotificationDto notification = objectMapper.readValue(
                payload,
                SmsNotificationDto.class
        );

        SmsRequest request = new SmsRequest();
        request.setPhoneNumber(notification.getPhoneNumber());
        request.setMessage(notification.getMessage());

        smsService.sendSms(request);

        log.info(
                "SMS notification processed from Kafka. Action id: {}, phone: {}",
                notification.getActionId(),
                notification.getPhoneNumber()
        );
    }
}