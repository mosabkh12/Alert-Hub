package com.alerthub.actionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActionScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ActionScheduler.class);

    private final ActionService actionService;

    public ActionScheduler(ActionService actionService) {
        this.actionService = actionService;
    }

    @Scheduled(cron = "0 0,30 * * * *")
    public void queueDueActionsEveryThirtyMinutes() {
        int queuedActions = actionService.queueDueActions();

        log.info(
                "Action scheduler completed. Queued {} action(s) to Kafka.",
                queuedActions
        );
    }
}