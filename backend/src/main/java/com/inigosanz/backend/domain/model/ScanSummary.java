package com.inigosanz.backend.domain.model;

import java.util.Map;
import java.util.Objects;

public class ScanSummary {

    private final Long scanId;
    private final long totalIssues;
    private final Map<String, Long> bySeverity;

    public ScanSummary(Long scanId, long totalIssues, Map<String, Long> bySeverity) {
        this.scanId = Objects.requireNonNull(scanId, "scanId is required");
        this.totalIssues = totalIssues;
        this.bySeverity = Objects.requireNonNull(bySeverity, "bySeverity is required");
    }

    public Long getScanId() {
        return scanId;
    }

    public long getTotalIssues() {
        return totalIssues;
    }

    public Map<String, Long> getBySeverity() {
        return bySeverity;
    }
}

