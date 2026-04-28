package com.slavacom.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Конфигурация логирования
 */
@Slf4j
@Configuration
public class LoggingConfiguration {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        createLogDirectories();
        logStartupInfo();
    }

    private void createLogDirectories() {
        try {
            String[] logDirs = {"logs", "logs/archive"};

            for (String dir : logDirs) {
                File logDir = new File(dir);
                if (!logDir.exists()) {
                    Files.createDirectories(Paths.get(dir));
                    log.info("Created log directory: {}", logDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create log directories", e);
        }
    }

    private void logStartupInfo() {
        log.info("=".repeat(80));
        log.info("USER SERVICE STARTED");
        log.info("Logging configuration:");
        log.info("- Controllers logs: logs/controllers.log");
        log.info("- Services logs: logs/services.log");
        log.info("- Kafka events: logs/kafka.log");
        log.info("- Errors: logs/errors.log");
        log.info("- General: logs/user-service.log");
        log.info("=".repeat(80));
    }
}
