package com.alerthub.userservice.config;

import com.alerthub.userservice.entity.Role;
import com.alerthub.userservice.entity.User;
import com.alerthub.userservice.entity.UserRole;
import com.alerthub.userservice.repository.RoleRepository;
import com.alerthub.userservice.repository.UserRepository;
import com.alerthub.userservice.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PermissionDataInitializer {

    private static final String READ_PERMISSION = "read";
    private static final String ADMIN_USERNAME = "admin";

    @Bean
    public CommandLineRunner initializePermissions(
            RoleRepository roleRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository
    ) {
        return args -> {
            List<String> permissions = List.of(
                    "createAction",
                    "updateAction",
                    "deleteAction",
                    "createMetric",
                    "updateMetric",
                    "deleteMetric",
                    "triggerScan",
                    "triggerProcess",
                    "triggerEvaluation",
                    READ_PERMISSION,
                    "createUser",
                    "deleteUser",
                    "grantPermission",
                    "revokePermission"
            );

            for (String permission : permissions) {
                if (roleRepository.findByRole(permission).isEmpty()) {
                    roleRepository.save(
                            Role.builder()
                                    .role(permission)
                                    .build()
                    );
                }
            }

            Role readRole = roleRepository.findByRole(READ_PERMISSION)
                    .orElseThrow(() -> new IllegalStateException(
                            "Read permission was not created"
                    ));

            for (User user : userRepository.findAll()) {
                grantPermissionIfMissing(
                        user.getId(),
                        readRole.getId(),
                        userRoleRepository
                );
            }

            userRepository.findByUsername(ADMIN_USERNAME)
                    .ifPresent(adminUser -> {
                        for (Role role : roleRepository.findAll()) {
                            grantPermissionIfMissing(
                                    adminUser.getId(),
                                    role.getId(),
                                    userRoleRepository
                            );
                        }
                    });
        };
    }

    private void grantPermissionIfMissing(
            Long userId,
            Long roleId,
            UserRoleRepository userRoleRepository
    ) {
        boolean alreadyGranted = userRoleRepository
                .findByUserIdAndRoleId(userId, roleId)
                .isPresent();

        if (!alreadyGranted) {
            userRoleRepository.save(
                    UserRole.builder()
                            .userId(userId)
                            .roleId(roleId)
                            .build()
            );
        }
    }
}