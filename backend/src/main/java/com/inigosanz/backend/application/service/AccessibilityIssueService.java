package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.port.in.ListIssuesByScanUseCase;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AccessibilityIssueService implements ListIssuesByScanUseCase {

    private final ScanRepositoryPort scanRepositoryPort;
    private final AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    public AccessibilityIssueService(
            ScanRepositoryPort scanRepositoryPort,
            AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort
    ) {
        this.scanRepositoryPort = scanRepositoryPort;
        this.accessibilityIssueRepositoryPort = accessibilityIssueRepositoryPort;
    }

    @Override
    public List<AccessibilityIssue> findByScanId(Long scanId) {
        Objects.requireNonNull(scanId, "scanId is required");

        scanRepositoryPort.findById(scanId)
                .orElseThrow(ScanNotFoundException::new);

        return accessibilityIssueRepositoryPort.findByScanId(scanId);
    }
}

