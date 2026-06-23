package com.alerthub.processorservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EmailNotificationProducer {

    private static final String EMAIL_TOPIC = "email-notifications";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EmailNotificationProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendEmailNotification(
            Long actionId,
            String recipient,
            String message
    ) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "actionId", actionId,
                            "recipient", recipient,
                            "message", message,
                            "createdAt", LocalDateTime.now().toString()
                    )
            );

            kafkaTemplate.send(EMAIL_TOPIC, payload);

        } catch (JsonProcessingException exception) {
            throw new RuntimeException(
                    "Failed to create email Kafka message",
                    exception
            );
        }
    }
}