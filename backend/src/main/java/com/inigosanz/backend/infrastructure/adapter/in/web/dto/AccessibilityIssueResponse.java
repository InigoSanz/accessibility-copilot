package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

public record AccessibilityIssueResponse(
        Long id,
        Long scanId,
        String ruleCode,
        String message,
        String severity,
        String wcagCriterion,
        String pageUrl,
        String htmlSnippet,
        String selector,
        String recommendation,
        String helpUrl
) {
}
