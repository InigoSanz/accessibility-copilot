package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessibilityIssueServiceTest {

    @Mock
    private ScanRepositoryPort scanRepositoryPort;

    @Mock
    private AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    @InjectMocks
    private AccessibilityIssueService accessibilityIssueService;

    @Test
    void shouldReturnIssuesWhenScanExists() {
        Long scanId = 1L;
        Scan scan = new Scan(scanId, 1L, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());
        AccessibilityIssue issue = new AccessibilityIssue(
                1L,
                scanId,
                "WCAG_1_1_1",
                "Image without alt text",
                "HIGH",
                "1.1.1",
                "https://example.com",
                "<img src=\"banner.jpg\">"
        );

        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(scan));
        when(accessibilityIssueRepositoryPort.findByScanId(scanId)).thenReturn(List.of(issue));

        List<AccessibilityIssue> result = accessibilityIssueService.findByScanId(scanId);

        verify(scanRepositoryPort, times(1)).findById(scanId);
        verify(accessibilityIssueRepositoryPort, times(1)).findByScanId(scanId);
        assertEquals(1, result.size());
        assertSame(issue, result.get(0));
    }

    @Test
    void shouldThrowScanNotFoundExceptionWhenScanDoesNotExist() {
        Long scanId = 999L;
        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.empty());

        ScanNotFoundException exception = assertThrows(
                ScanNotFoundException.class,
                () -> accessibilityIssueService.findByScanId(scanId)
        );

        verify(scanRepositoryPort, times(1)).findById(scanId);
        assertEquals("Scan not found", exception.getMessage());
    }
}

