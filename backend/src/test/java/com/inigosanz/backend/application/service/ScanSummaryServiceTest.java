package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.model.ScanSummary;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanSummaryServiceTest {

    @Mock
    private ScanRepositoryPort scanRepositoryPort;

    @Mock
    private AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    @InjectMocks
    private ScanSummaryService scanSummaryService;

    @Test
    void shouldReturnSummaryWithTotalIssuesAndGroupedBySeverity() {
        Long scanId = 1L;
        Scan scan = new Scan(scanId, 1L, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        List<AccessibilityIssue> issues = List.of(
                new AccessibilityIssue(
                        null,
                        scanId,
                        "image-alt",
                        "msg1",
                        "serious",
                        "1.1.1",
                        "https://example.com",
                        "<img>",
                        "img.hero-banner",
                        "Add a meaningful alt attribute describing the image purpose.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html"
                ),
                new AccessibilityIssue(
                        null,
                        scanId,
                        "color-contrast",
                        "msg2",
                        "serious",
                        "1.4.3",
                        "https://example.com",
                        "<button>",
                        "button.submit-primary",
                        "Increase text and background contrast to meet at least a 4.5:1 ratio.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html"
                ),
                new AccessibilityIssue(
                        null,
                        scanId,
                        "label",
                        "msg3",
                        "moderate",
                        "3.3.2",
                        "https://example.com/contact",
                        "<input>",
                        "form#contact input[type='email']",
                        "Associate the input with a visible label using the for and id attributes.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/labels-or-instructions.html"
                )
        );

        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(scan));
        when(accessibilityIssueRepositoryPort.findByScanId(scanId)).thenReturn(issues);

        ScanSummary summary = scanSummaryService.getSummary(scanId);

        verify(scanRepositoryPort, times(1)).findById(scanId);
        verify(accessibilityIssueRepositoryPort, times(1)).findByScanId(scanId);

        assertEquals(scanId, summary.getScanId());
        assertEquals(3L, summary.getTotalIssues());
        assertEquals(2L, summary.getBySeverity().get("serious"));
        assertEquals(1L, summary.getBySeverity().get("moderate"));
    }

    @Test
    void shouldThrowScanNotFoundExceptionWhenScanDoesNotExist() {
        Long scanId = 999L;
        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.empty());

        ScanNotFoundException exception = assertThrows(
                ScanNotFoundException.class,
                () -> scanSummaryService.getSummary(scanId)
        );

        verify(scanRepositoryPort, times(1)).findById(scanId);
        assertEquals("Scan not found", exception.getMessage());
    }
}

