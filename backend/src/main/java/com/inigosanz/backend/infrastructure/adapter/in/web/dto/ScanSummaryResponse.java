package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

import java.util.Map;

public record ScanSummaryResponse(
        Long scanId,
        long totalIssues,
        Map<String, Long> bySeverity
) {
}

