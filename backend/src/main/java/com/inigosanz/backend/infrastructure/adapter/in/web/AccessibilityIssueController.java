package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.port.in.ListIssuesByScanUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.AccessibilityIssueResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scans/{scanId}/issues")
public class AccessibilityIssueController {

    private final ListIssuesByScanUseCase listIssuesByScanUseCase;
    private final AccessibilityIssueWebMapper accessibilityIssueWebMapper;

    public AccessibilityIssueController(
            ListIssuesByScanUseCase listIssuesByScanUseCase,
            AccessibilityIssueWebMapper accessibilityIssueWebMapper
    ) {
        this.listIssuesByScanUseCase = listIssuesByScanUseCase;
        this.accessibilityIssueWebMapper = accessibilityIssueWebMapper;
    }

    @GetMapping
    public ResponseEntity<List<AccessibilityIssueResponse>> findByScanId(@PathVariable Long scanId) {
        List<AccessibilityIssue> issues = listIssuesByScanUseCase.findByScanId(scanId);
        List<AccessibilityIssueResponse> response = accessibilityIssueWebMapper.toResponseList(issues);
        return ResponseEntity.ok(response);
    }
}

