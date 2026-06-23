package com.alerthub.actionservice.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import com.alerthub.actionservice.entity.Action;
import com.alerthub.actionservice.repository.ActionRepository;
import org.springframework.stereotype.Service;
import com.alerthub.actionservice.kafka.ActionJobProducer;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final ActionJobProducer actionJobProducer;
    public ActionService(
            ActionRepository actionRepository,
            ActionJobProducer actionJobProducer
    ) {
        this.actionRepository = actionRepository;
        this.actionJobProducer = actionJobProducer;
    }
    public List<Action> getAllActions() {
        return actionRepository.findByIsDeletedFalse();
    }

    public Action getActionById(Long id) {
        return actionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Action not found with id: " + id));
    }

    public List<Action> getActionsByUserId(Long userId) {
        return actionRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    public Action createAction(Action action) {
        return actionRepository.save(action);
    }

    public Action updateAction(Long id, Action updatedAction) {
        Action existingAction = getActionById(id);

        existingAction.setUserId(updatedAction.getUserId());
        existingAction.setName(updatedAction.getName());
        existingAction.setConditionJson(updatedAction.getConditionJson());
        existingAction.setTo(updatedAction.getTo());
        existingAction.setActionType(updatedAction.getActionType());
        existingAction.setRunOnTime(updatedAction.getRunOnTime());
        existingAction.setRunOnDay(updatedAction.getRunOnDay());
        existingAction.setMessage(updatedAction.getMessage());
        existingAction.setIsEnabled(updatedAction.getIsEnabled());

        return actionRepository.save(existingAction);
    }

    public Action enableAction(Long id) {
        Action action = getActionById(id);
        action.setIsEnabled(true);
        return actionRepository.save(action);
    }

    public Action disableAction(Long id) {
        Action action = getActionById(id);
        action.setIsEnabled(false);
        return actionRepository.save(action);
    }

    public void deleteAction(Long id) {
        Action action = getActionById(id);

        /*
         * Soft delete:
         * לפי המסמך לא מוחקים באמת את ה־Action מה־DB.
         */
        action.setIsDeleted(true);
        actionRepository.save(action);
    }
    public void queueActionJob(Long actionId) {
    Action action = getActionById(actionId);

    if (Boolean.TRUE.equals(action.getIsDeleted())) {
        throw new RuntimeException("Deleted action cannot be queued");
    }

    if (!Boolean.TRUE.equals(action.getIsEnabled())) {
        throw new RuntimeException("Disabled action cannot be queued");
    }

    actionJobProducer.sendActionJob(actionId);
    }
    public int queueDueActions() {
    LocalDateTime now = LocalDateTime.now()
            .withSecond(0)
            .withNano(0);

    int queuedActions = 0;

    List<Action> activeActions =
            actionRepository.findByIsEnabledTrueAndIsDeletedFalse();

    for (Action action : activeActions) {
        if (!isDueNow(action, now)) {
            continue;
        }

        actionJobProducer.sendActionJob(action.getId());

        action.setLastRun(now);
        actionRepository.save(action);

        queuedActions++;
    }

        return queuedActions;
    }

    private boolean isDueNow(Action action, LocalDateTime now) {
        if (!matchesDay(action.getRunOnDay(), now.getDayOfWeek())) {
            return false;
        }

        if (action.getRunOnTime() == null) {
            return false;
        }

        boolean sameScheduledTime =
                action.getRunOnTime().getHour() == now.getHour()
                        && action.getRunOnTime().getMinute() == now.getMinute();

        if (!sameScheduledTime) {
            return false;
        }

        if (action.getLastRun() == null) {
            return true;
        }

        return !action.getLastRun()
                .withSecond(0)
                .withNano(0)
                .equals(now);
    }

    private boolean matchesDay(String runOnDay, DayOfWeek currentDay) {
        if (runOnDay == null || runOnDay.isBlank()) {
            return false;
        }

        if ("All".equalsIgnoreCase(runOnDay)) {
            return true;
        }

        return runOnDay.equalsIgnoreCase(currentDay.name());
    }
}