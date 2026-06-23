package com.alerthub.processorservice.kafka;

import com.alerthub.processorservice.service.ProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ActionJobConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ActionJobConsumer.class);

    private final ProcessorService processorService;

    public ActionJobConsumer(ProcessorService processorService) {
        this.processorService = processorService;
    }

    @KafkaListener(
            topics = "action-jobs",
            groupId = "processor-service"
    )
    public void consumeActionJob(String actionIdText) {
        try {
            Long actionId = Long.parseLong(actionIdText);

            log.info(
                    "Received action job from Kafka. Action id: {}",
                    actionId
            );

            processorService.processAction(actionId);

            log.info(
                    "Action job processed successfully. Action id: {}",
                    actionId
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to process Kafka action job: {}",
                    actionIdText,
                    exception
            );
        }
    }
}