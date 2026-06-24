package com.alerthub.actionservice.controller;

import com.alerthub.actionservice.entity.Action;
import com.alerthub.actionservice.security.JwtPermissionValidator;
import com.alerthub.actionservice.service.ActionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionService actionService;
    private final JwtPermissionValidator jwtPermissionValidator;

    public ActionController(
            ActionService actionService,
            JwtPermissionValidator jwtPermissionValidator
    ) {
        this.actionService = actionService;
        this.jwtPermissionValidator = jwtPermissionValidator;
    }

    @GetMapping
    public List<Action> getAllActions() {
        return actionService.getAllActions();
    }

    @GetMapping("/{id}")
    public Action getActionById(@PathVariable Long id) {
        return actionService.getActionById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Action> getActionsByUserId(@PathVariable Long userId) {
        return actionService.getActionsByUserId(userId);
    }

    @PostMapping
    public Action createAction(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorizationHeader,
            @Valid @RequestBody Action action
    ) {
        jwtPermissionValidator.requirePermission(
                authorizationHeader,
                "createAction"
        );

        return actionService.createAction(action);
    }

    @PutMapping("/{id}")
    public Action updateAction(
            @PathVariable Long id,
            @Valid @RequestBody Action action
    ) {
        return actionService.updateAction(id, action);
    }

    @PatchMapping("/{id}/enable")
    public Action enableAction(@PathVariable Long id) {
        return actionService.enableAction(id);
    }

    @PatchMapping("/{id}/disable")
    public Action disableAction(@PathVariable Long id) {
        return actionService.disableAction(id);
    }

    @DeleteMapping("/{id}")
    public String deleteAction(@PathVariable Long id) {
        actionService.deleteAction(id);
        return "Action deleted successfully";
    }

    @PostMapping("/{id}/queue")
    public String queueActionJob(@PathVariable Long id) {
        actionService.queueActionJob(id);

        return "Action job sent to Kafka successfully";
    }
}