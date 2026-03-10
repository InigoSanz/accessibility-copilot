package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.AccessibilityIssue;

import java.util.List;

public interface ListIssuesByScanUseCase {

    List<AccessibilityIssue> findByScanId(Long scanId);
}

