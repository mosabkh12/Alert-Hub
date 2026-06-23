package com.alerthub.processorservice.service;

import com.alerthub.processorservice.dto.*;
import com.alerthub.processorservice.kafka.EmailNotificationProducer;
import com.alerthub.processorservice.kafka.SmsNotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessorServiceTest {

    private RestTemplate restTemplate;
    private EmailNotificationProducer emailNotificationProducer;
    private SmsNotificationProducer smsNotificationProducer;
    private LoggerClient loggerClient;
    private ProcessorService processorService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        emailNotificationProducer = mock(EmailNotificationProducer.class);
        smsNotificationProducer = mock(SmsNotificationProducer.class);
        loggerClient = mock(LoggerClient.class);

        processorService = new ProcessorService(
                restTemplate,
                emailNotificationProducer,
                smsNotificationProducer,
                loggerClient
        );

        setPrivateField(processorService, "actionServiceUrl", "http://localhost:8083");
        setPrivateField(processorService, "metricServiceUrl", "http://localhost:8084");
        setPrivateField(processorService, "loaderServiceUrl", "http://localhost:8085");
    }

    @Test
    void processAction_whenEmailAndConditionTrue_shouldSendEmailToKafkaAndLog() {
        ActionDto action = createAction("EMAIL", true, false);
        MetricDto metric = createMetric();

        PlatformInformationDto info = new PlatformInformationDto();
        info.setTimestamp(OffsetDateTime.now().toString());

        when(restTemplate.getForObject(
                "http://localhost:8083/api/actions/1",
                ActionDto.class
        )).thenReturn(action);

        when(restTemplate.getForObject(
                "http://localhost:8084/api/metrics/1",
                MetricDto.class
        )).thenReturn(metric);

        when(restTemplate.getForObject(
                "http://localhost:8085/api/platform-information/label/bug",
                PlatformInformationDto[].class
        )).thenReturn(new PlatformInformationDto[]{info});

        ProcessResultDto result = processorService.processAction(1L);

        assertTrue(result.getConditionSatisfied());
        assertEquals("EMAIL", result.getActionType());
        assertEquals("ameen@gmail.com", result.getRecipient());

        verify(emailNotificationProducer, times(1)).sendEmailNotification(
                1L,
                "ameen@gmail.com",
                "Need to fix"
        );

        verify(smsNotificationProducer, never()).sendSmsNotification(
                anyLong(),
                anyString(),
                anyString()
        );

        verify(loggerClient, times(1)).sendLog(
                "INFO",
                "Email notification sent to Kafka topic"
        );
    }

    @Test
    void processAction_whenSmsAndConditionTrue_shouldSendSmsToKafkaAndLog() {
        ActionDto action = createAction("SMS", true, false);
        action.setTo("0509999999");
        action.setMessage("Help me by SMS");

        MetricDto metric = createMetric();

        PlatformInformationDto info = new PlatformInformationDto();
        info.setTimestamp(OffsetDateTime.now().toString());

        when(restTemplate.getForObject(
                "http://localhost:8083/api/actions/2",
                ActionDto.class
        )).thenReturn(action);

        when(restTemplate.getForObject(
                "http://localhost:8084/api/metrics/1",
                MetricDto.class
        )).thenReturn(metric);

        when(restTemplate.getForObject(
                "http://localhost:8085/api/platform-information/label/bug",
                PlatformInformationDto[].class
        )).thenReturn(new PlatformInformationDto[]{info});

        ProcessResultDto result = processorService.processAction(2L);

        assertTrue(result.getConditionSatisfied());
        assertEquals("SMS", result.getActionType());
        assertEquals("0509999999", result.getRecipient());

        verify(smsNotificationProducer, times(1)).sendSmsNotification(
                1L,
                "0509999999",
                "Help me by SMS"
        );

        verify(emailNotificationProducer, never()).sendEmailNotification(
                anyLong(),
                anyString(),
                anyString()
        );

        verify(loggerClient, times(1)).sendLog(
                "INFO",
                "SMS notification sent to Kafka topic"
        );
    }

    @Test
    void processAction_whenActionDisabled_shouldNotSendAnything() {
        ActionDto action = createAction("EMAIL", false, false);

        when(restTemplate.getForObject(
                "http://localhost:8083/api/actions/3",
                ActionDto.class
        )).thenReturn(action);

        ProcessResultDto result = processorService.processAction(3L);

        assertFalse(result.getConditionSatisfied());
        assertEquals("Action is disabled", result.getMessage());

        verifyNoInteractions(
                emailNotificationProducer,
                smsNotificationProducer,
                loggerClient
        );
    }

    @Test
    void processAction_whenActionDeleted_shouldNotSendAnything() {
        ActionDto action = createAction("EMAIL", true, true);

        when(restTemplate.getForObject(
                "http://localhost:8083/api/actions/4",
                ActionDto.class
        )).thenReturn(action);

        ProcessResultDto result = processorService.processAction(4L);

        assertFalse(result.getConditionSatisfied());
        assertEquals("Action is deleted", result.getMessage());

        verifyNoInteractions(
                emailNotificationProducer,
                smsNotificationProducer,
                loggerClient
        );
    }

    private ActionDto createAction(
            String actionType,
            boolean enabled,
            boolean deleted
    ) {
        ActionDto action = new ActionDto();
        action.setId(1L);
        action.setActionType(actionType);
        action.setTo("ameen@gmail.com");
        action.setMessage("Need to fix");
        action.setConditionJson("[[1]]");
        action.setIsEnabled(enabled);
        action.setIsDeleted(deleted);
        return action;
    }

    private MetricDto createMetric() {
        MetricDto metric = new MetricDto();
        metric.setId(1L);
        metric.setLabel("bug");
        metric.setThreshold(1.0);
        metric.setTimeFrameHours(24);
        return metric;
    }

    private void setPrivateField(
            Object target,
            String fieldName,
            Object value
    ) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}