package com.alerthub.loaderservice.repository;

import com.alerthub.loaderservice.entity.ScannedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long> {

    boolean existsByProviderAndFileName(String provider, String fileName);
}