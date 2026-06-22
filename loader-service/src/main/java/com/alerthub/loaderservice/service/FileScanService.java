package com.alerthub.loaderservice.service;

import com.alerthub.loaderservice.entity.PlatformInformation;
import com.alerthub.loaderservice.entity.ScannedFile;
import com.alerthub.loaderservice.repository.PlatformInformationRepository;
import com.alerthub.loaderservice.repository.ScannedFileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FileScanService {

    private final ScannedFileRepository scannedFileRepository;
    private final PlatformInformationRepository platformInformationRepository;
    private final ObjectMapper objectMapper;

    @Value("${loader.data-path:../data}")
    private String dataPath;

    public FileScanService(
            ScannedFileRepository scannedFileRepository,
            PlatformInformationRepository platformInformationRepository,
            ObjectMapper objectMapper
    ) {
        this.scannedFileRepository = scannedFileRepository;
        this.platformInformationRepository = platformInformationRepository;
        this.objectMapper = objectMapper;
    }

    public int scanGitHubFiles() {
        return scanProviderFolder("github", "GitHub");
    }
    public int scanJiraFiles() {
        return scanProviderFolder("jira", "Jira");
    }
    public int scanClickUpFiles() {
        return scanProviderFolder("clickup", "ClickUp");
    }
    public int scanAllFiles() {
    int githubRecords = scanGitHubFiles();
    int jiraRecords = scanJiraFiles();
    int clickUpRecords = scanClickUpFiles();

    return githubRecords + jiraRecords + clickUpRecords;


}

    private int scanProviderFolder(String folderName, String providerName) {
        Path providerFolder = Path.of(dataPath, folderName);

        if (!Files.exists(providerFolder)) {
            return 0;
        }

        int importedRecords = 0;

        try (var files = Files.list(providerFolder)) {
            List<Path> jsonFiles = files
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".json"))
                    .toList();

            for (Path file : jsonFiles) {
                String fileName = file.getFileName().toString();

                if (scannedFileRepository.existsByProviderAndFileName(providerName, fileName)) {
                    continue;
                }

                List<Map<String, Object>> rows = objectMapper.readValue(
                        file.toFile(),
                        new TypeReference<>() {}
                );

                for (Map<String, Object> row : rows) {
                    PlatformInformation info = mapRowToPlatformInformation(
                            row,
                            providerName,
                            fileName
                    );

                    platformInformationRepository.save(info);
                    importedRecords++;
                }

                ScannedFile scannedFile = ScannedFile.builder()
                        .provider(providerName)
                        .fileName(fileName)
                        .scannedAt(LocalDateTime.now())
                        .status("SUCCESS")
                        .build();

                scannedFileRepository.save(scannedFile);
            }

        } catch (IOException exception) {
            throw new RuntimeException("Failed to scan " + providerName + " files", exception);
        }

        return importedRecords;
    }
    private PlatformInformation mapRowToPlatformInformation(
        Map<String, Object> row,
        String providerName,
        String fileName
) {
    PlatformInformation info = new PlatformInformation();

    if ("GitHub".equals(providerName)) {
        info.setOwnerId(stringValue(row.get("manager_id")));
        info.setProject(stringValue(row.get("projects")));
        info.setTag(stringValue(row.get("assignee")));
        info.setLabel(stringValue(row.get("label")));
        info.setDeveloperId(stringValue(row.get("developer_id")));
        info.setTaskNumber(stringValue(row.get("issue")));
        info.setEnvironment(stringValue(row.get("environment")));
        info.setUserStory(stringValue(row.get("user_story")));
        info.setTaskPoint(numberValue(row.get("point")));
        info.setSprint(stringValue(row.get("sprint")));
    }


    if ("Jira".equals(providerName)) {
        info.setOwnerId(stringValue(row.get("reporter_id")));
        info.setProject(stringValue(row.get("project_key")));
        info.setTag(stringValue(row.get("assignee_name")));
        info.setLabel(stringValue(row.get("issue_type")));
        info.setDeveloperId(stringValue(row.get("assignee_id")));
        info.setTaskNumber(stringValue(row.get("issue_key")));
        info.setEnvironment(stringValue(row.get("environment_name")));
        info.setUserStory(stringValue(row.get("story_summary")));
        info.setTaskPoint(numberValue(row.get("story_points")));
        info.setSprint(stringValue(row.get("sprint_name")));
    }
    if ("ClickUp".equals(providerName)) {
    info.setOwnerId(stringValue(row.get("creator_id")));
    info.setProject(stringValue(row.get("workspace_name")));
    info.setTag(stringValue(row.get("assignee_username")));
    info.setLabel(stringValue(row.get("task_status")));
    info.setDeveloperId(stringValue(row.get("assignee_id")));
    info.setTaskNumber(stringValue(row.get("task_id")));
    info.setEnvironment(stringValue(row.get("task_environment")));
    info.setUserStory(stringValue(row.get("task_description")));
    info.setTaskPoint(numberValue(row.get("estimate_points")));
    info.setSprint(stringValue(row.get("sprint_title")));
    }

    info.setProvider(providerName);
    info.setSourceFileName(fileName);

    return info;
}

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer numberValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.parseInt(String.valueOf(value));
    }
}