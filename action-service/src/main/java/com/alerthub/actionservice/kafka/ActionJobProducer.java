package com.alerthub.actionservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActionJobProducer {

    private static final String ACTION_JOBS_TOPIC = "action-jobs";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ActionJobProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendActionJob(Long actionId) {
        kafkaTemplate.send(
                ACTION_JOBS_TOPIC,
                actionId.toString()
        );
    }
}