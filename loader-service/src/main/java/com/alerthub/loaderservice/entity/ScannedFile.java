package com.alerthub.loaderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "scanned_files",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_scanned_file_provider_name",
                        columnNames = {"provider", "file_name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScannedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @Column(nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        if (scannedAt == null) {
            scannedAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "SUCCESS";
        }
    }
}