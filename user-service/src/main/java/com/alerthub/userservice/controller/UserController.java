package com.alerthub.userservice.controller;

import com.alerthub.userservice.dto.LoginRequest;
import com.alerthub.userservice.dto.UserResponse;
import com.alerthub.userservice.entity.User;
import com.alerthub.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alerthub.userservice.dto.UserSecurityContextResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody User user
    ) {
        User savedUser = userService.createUser(user);

        return ResponseEntity.ok(toUserResponse(savedUser));
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId
    ) {
        User user = userService.getUserById(userId);

        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<UserResponse> authenticate(
            @RequestBody LoginRequest request
    ) {
        User user = userService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone()
        );
    }
    @GetMapping("/{userId}/security-context")
    public ResponseEntity<UserSecurityContextResponse> getSecurityContext(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userService.getSecurityContext(userId)
        );
    }
}