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
        try {
            Project project = projectRepositoryPort.findById(projectId)
                    .orElseThrow(() -> new IllegalStateException("Project not found for scan execution"));

            Scan currentScan = scanRepositoryPort.findById(scanId)
                    .orElseThrow(() -> new IllegalStateException("Scan not found for execution"));

            List<AccessibilityIssue> issues = webAccessibilityScannerPort.scan(scanId, project.getRootUrl());
            if (!issues.isEmpty()) {
                accessibilityIssueRepositoryPort.saveAll(issues);
            }

            Scan completedScan = new Scan(
                    currentScan.getId(),
                    currentScan.getProjectId(),
                    ScanStatus.COMPLETED,
                    currentScan.getStartedAt(),
                    LocalDateTime.now(),
                    null
            );
            scanRepositoryPort.save(completedScan);
        } catch (Exception exception) {
            LOGGER.error("Scan execution failed for scanId={}", scanId, exception);
            markAsFailed(scanId, exception);
        }
    }

    private void markAsFailed(Long scanId, Exception exception) {
        scanRepositoryPort.findById(scanId).ifPresent(scan -> {
            Scan failedScan = new Scan(
                    scan.getId(),
                    scan.getProjectId(),
                    ScanStatus.FAILED,
                    scan.getStartedAt(),
                    LocalDateTime.now(),
                    truncateErrorMessage(exception.getMessage())
            );
            scanRepositoryPort.save(failedScan);
        });
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

