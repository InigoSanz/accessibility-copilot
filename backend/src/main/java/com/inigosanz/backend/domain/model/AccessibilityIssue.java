package com.inigosanz.backend.domain.model;

import java.util.Objects;

public class AccessibilityIssue {

    private final Long id;
    private final Long scanId;
    private final String ruleCode;
    private final String message;
    private final String severity;
    private final String wcagCriterion;
    private final String pageUrl;
    private final String htmlSnippet;

    public AccessibilityIssue(
            Long id,
            Long scanId,
            String ruleCode,
            String message,
            String severity,
            String wcagCriterion,
            String pageUrl,
            String htmlSnippet
    ) {
        this.id = id;
        this.scanId = Objects.requireNonNull(scanId, "scanId is required");
        this.ruleCode = Objects.requireNonNull(ruleCode, "ruleCode is required");
        this.message = Objects.requireNonNull(message, "message is required");
        this.severity = Objects.requireNonNull(severity, "severity is required");
        this.wcagCriterion = wcagCriterion;
        this.pageUrl = pageUrl;
        this.htmlSnippet = htmlSnippet;
    }

    public Long getId() {
        return id;
    }

    public Long getScanId() {
        return scanId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getWcagCriterion() {
        return wcagCriterion;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getHtmlSnippet() {
        return htmlSnippet;
    }
}

