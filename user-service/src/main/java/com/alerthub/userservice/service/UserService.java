package com.alerthub.userservice.service;

import com.alerthub.userservice.dto.UserSecurityContextResponse;
import com.alerthub.userservice.entity.Role;
import com.alerthub.userservice.entity.User;
import com.alerthub.userservice.entity.UserRole;
import com.alerthub.userservice.repository.RoleRepository;
import com.alerthub.userservice.repository.UserRepository;
import com.alerthub.userservice.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

       public User createUser(User user) {
        validateNewUser(user);

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User savedUser = userRepository.save(user);

        Role readRole = roleRepository.findByRole("read")
                .orElseThrow(() -> new IllegalStateException(
                        "Read permission does not exist"
                ));

        userRoleRepository.save(
                UserRole.builder()
                        .userId(savedUser.getId())
                        .roleId(readRole.getId())
                        .build()
        );

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId
                ));
    }

    public User authenticate(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid username or password"
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        return user;
    }

    public UserSecurityContextResponse getSecurityContext(Long userId) {
        User user = getUserById(userId);

        List<String> permissions = userRoleRepository
                .findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Role not found: " + roleId
                        )))
                .map(Role::getRole)
                .toList();

        return new UserSecurityContextResponse(
                user.getId(),
                user.getUsername(),
                permissions
        );
    }

    private void validateNewUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }
}