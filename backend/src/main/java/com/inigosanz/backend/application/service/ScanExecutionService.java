package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.domain.port.out.WebAccessibilityScannerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScanExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanExecutionService.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ScanRepositoryPort scanRepositoryPort;
    private final AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;
    private final WebAccessibilityScannerPort webAccessibilityScannerPort;

    public ScanExecutionService(
            ProjectRepositoryPort projectRepositoryPort,
            ScanRepositoryPort scanRepositoryPort,
            AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort,
            WebAccessibilityScannerPort webAccessibilityScannerPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.scanRepositoryPort = scanRepositoryPort;
        this.accessibilityIssueRepositoryPort = accessibilityIssueRepositoryPort;
        this.webAccessibilityScannerPort = webAccessibilityScannerPort;
    }

    @Async("scanTaskExecutor")
    public void executeAsync(Long scanId, Long projectId) {
        Optional<Scan> maybeScan = scanRepositoryPort.findById(scanId);
        if (maybeScan.isEmpty()) {
            LOGGER.warn("Skipping scan execution because scanId={} was not found", scanId);
            return;
        }

        Scan currentScan = maybeScan.get();
        if (currentScan.getStatus() != ScanStatus.RUNNING) {
            LOGGER.warn(
                    "Skipping scan execution because scanId={} is in status={} instead of RUNNING",
                    scanId,
                    currentScan.getStatus()
            );
            return;
        }

        try {
            Project project = projectRepositoryPort.findById(projectId)
                    .orElseThrow(() -> new IllegalStateException("Project not found for scan execution"));

            List<AccessibilityIssue> issues = webAccessibilityScannerPort.scan(scanId, project.getRootUrl());
            if (!issues.isEmpty()) {
                accessibilityIssueRepositoryPort.saveAll(issues);
            }

            boolean markedCompleted = scanRepositoryPort.markCompleted(scanId, LocalDateTime.now());
            if (!markedCompleted) {
                LOGGER.warn(
                        "Scan status transition to COMPLETED was skipped for scanId={} because it is no longer RUNNING",
                        scanId
                );
            }
        } catch (Exception exception) {
            LOGGER.error("Scan execution failed for scanId={}", scanId, exception);
            markAsFailed(scanId, truncateErrorMessage(exception.getMessage()));
        }
    }

    private void markAsFailed(Long scanId, String errorMessage) {
        boolean markedFailed = scanRepositoryPort.markFailed(scanId, LocalDateTime.now(), errorMessage);
        if (!markedFailed) {
            LOGGER.warn(
                    "Scan status transition to FAILED was skipped for scanId={} because it is no longer RUNNING",
                    scanId
            );
        }
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Unexpected scan execution error";
        }
        if (errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}

