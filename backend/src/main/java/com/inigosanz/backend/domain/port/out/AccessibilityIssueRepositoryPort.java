package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.AccessibilityIssue;

import java.util.List;

public interface AccessibilityIssueRepositoryPort {

    List<AccessibilityIssue> findByScanId(Long scanId);

    List<AccessibilityIssue> saveAll(List<AccessibilityIssue> issues);
}

