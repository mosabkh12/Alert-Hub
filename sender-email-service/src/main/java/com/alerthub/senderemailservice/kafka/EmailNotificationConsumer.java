package com.alerthub.senderemailservice.kafka;

import com.alerthub.senderemailservice.dto.EmailNotificationDto;
import com.alerthub.senderemailservice.dto.EmailRequest;
import com.alerthub.senderemailservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(EmailNotificationConsumer.class);

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    public EmailNotificationConsumer(
            ObjectMapper objectMapper,
            EmailService emailService
    ) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "email-notifications",
            groupId = "sender-email-service"
    )
    public void consumeEmailNotification(String payload) {
        try {
            EmailNotificationDto notification =
                    objectMapper.readValue(
                            payload,
                            EmailNotificationDto.class
                    );

            EmailRequest request = new EmailRequest();
            request.setRecipient(notification.getRecipient());
            request.setMessage(notification.getMessage());

            emailService.sendEmail(request);

            log.info(
                    "Email notification processed from Kafka. Action id: {}, recipient: {}",
                    notification.getActionId(),
                    notification.getRecipient()
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to process email notification from Kafka. Payload: {}",
                    payload,
                    exception
            );
        }
    }
}