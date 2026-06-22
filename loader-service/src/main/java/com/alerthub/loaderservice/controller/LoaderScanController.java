package com.alerthub.loaderservice.controller;

import com.alerthub.loaderservice.service.FileScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/loader")
public class LoaderScanController {

    private final FileScanService fileScanService;

    public LoaderScanController(FileScanService fileScanService) {
        this.fileScanService = fileScanService;
    }

    @PostMapping("/scan/github")
    public ResponseEntity<Map<String, Object>> scanGitHubFiles() {
        int importedRecords = fileScanService.scanGitHubFiles();

        return ResponseEntity.ok(
                Map.of(
                        "provider", "GitHub",
                        "importedRecords", importedRecords,
                        "message", "GitHub scan completed successfully"
                )
        );
    }

    @PostMapping("/scan/jira")
    public ResponseEntity<Map<String, Object>> scanJiraFiles() {
        int importedRecords = fileScanService.scanJiraFiles();

        return ResponseEntity.ok(
                Map.of(
                        "provider", "Jira",
                        "importedRecords", importedRecords,
                        "message", "Jira scan completed successfully"
                )
        );
    }
    @PostMapping("/scan/clickup")
    public ResponseEntity<Map<String, Object>> scanClickUpFiles() {
      int importedRecords = fileScanService.scanClickUpFiles();

        return ResponseEntity.ok(
                Map.of(
                        "provider", "ClickUp",
                        "importedRecords", importedRecords,
                        "message", "ClickUp scan completed successfully"
                )
        );
    }
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanAllFiles() {
        int importedRecords = fileScanService.scanAllFiles();

        return ResponseEntity.ok(
                Map.of(
                        "provider", "ALL",
                        "importedRecords", importedRecords,
                        "message", "All platform scans completed successfully"
                )
        );
    }
}