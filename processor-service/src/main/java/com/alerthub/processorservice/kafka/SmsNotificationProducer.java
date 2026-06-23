package com.alerthub.processorservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SmsNotificationProducer {

    private static final String SMS_TOPIC = "sms-notifications";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SmsNotificationProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSmsNotification(
            Long actionId,
            String phoneNumber,
            String message
    ) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "actionId", actionId,
                            "phoneNumber", phoneNumber,
                            "message", message,
                            "createdAt", LocalDateTime.now().toString()
                    )
            );

            kafkaTemplate.send(SMS_TOPIC, payload);

        } catch (JsonProcessingException exception) {
            throw new RuntimeException(
                    "Failed to create SMS Kafka message",
                    exception
            );
        }
    }
}