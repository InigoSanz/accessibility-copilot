package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.AccessibilityIssue;

import java.util.List;

public interface WebAccessibilityScannerPort {

    List<AccessibilityIssue> scan(Long scanId, String pageUrl);
}

