package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.ScanSummary;
import com.inigosanz.backend.domain.port.in.GetScanSummaryUseCase;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ScanSummaryService implements GetScanSummaryUseCase {

    private final ScanRepositoryPort scanRepositoryPort;
    private final AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    public ScanSummaryService(
            ScanRepositoryPort scanRepositoryPort,
            AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort
    ) {
        this.scanRepositoryPort = scanRepositoryPort;
        this.accessibilityIssueRepositoryPort = accessibilityIssueRepositoryPort;
    }

    @Override
    public ScanSummary getSummary(Long scanId) {
        Objects.requireNonNull(scanId, "scanId is required");

        scanRepositoryPort.findById(scanId)
                .orElseThrow(ScanNotFoundException::new);

        List<AccessibilityIssue> issues = accessibilityIssueRepositoryPort.findByScanId(scanId);

        Map<String, Long> bySeverity = issues.stream()
                .collect(Collectors.groupingBy(AccessibilityIssue::getSeverity, LinkedHashMap::new, Collectors.counting()));

        return new ScanSummary(scanId, issues.size(), bySeverity);
    }
}

