package com.alerthub.loaderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoaderScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(LoaderScheduler.class);

    private final FileScanService fileScanService;

    public LoaderScheduler(FileScanService fileScanService) {
        this.fileScanService = fileScanService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scanAllPlatformsEveryMinuteForTesting() {
        int importedRecords = fileScanService.scanAllFiles();

        log.info(
                "Automatic Loader scan completed. Imported {} new record(s).",
                importedRecords
        );
    }
}