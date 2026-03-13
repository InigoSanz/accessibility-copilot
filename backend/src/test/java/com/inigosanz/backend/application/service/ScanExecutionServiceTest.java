package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.domain.port.out.WebAccessibilityScannerPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanExecutionServiceTest {

    @Mock
    private ProjectRepositoryPort projectRepositoryPort;

    @Mock
    private ScanRepositoryPort scanRepositoryPort;

    @Mock
    private AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    @Mock
    private WebAccessibilityScannerPort webAccessibilityScannerPort;

    @InjectMocks
    private ScanExecutionService scanExecutionService;

    @Test
    void shouldMarkScanCompletedAndPersistRealIssuesWhenScannerSucceeds() {
        Long scanId = 10L;
        Long projectId = 2L;
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(5);

        Project project = new Project(projectId, "Accessibility Copilot", "https://example.com", LocalDateTime.now());
        Scan runningScan = new Scan(scanId, projectId, ScanStatus.RUNNING, startedAt, null, null);

        List<AccessibilityIssue> issues = List.of(
                new AccessibilityIssue(
                        null,
                        scanId,
                        "image-alt",
                        "Image elements must have alternative text",
                        "serious",
                        "wcag111",
                        "https://example.com",
                        "<img src='hero.jpg'>",
                        "img.hero",
                        "Add meaningful alt text",
                        "https://example.com/help"
                )
        );

        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.of(project));
        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(runningScan));
        when(webAccessibilityScannerPort.scan(scanId, project.getRootUrl())).thenReturn(issues);
        when(scanRepositoryPort.markCompleted(eq(scanId), any(LocalDateTime.class))).thenReturn(true);

        scanExecutionService.executeAsync(scanId, projectId);

        verify(projectRepositoryPort, times(1)).findById(projectId);
        verify(scanRepositoryPort, times(1)).findById(scanId);
        verify(webAccessibilityScannerPort, times(1)).scan(scanId, project.getRootUrl());
        verify(accessibilityIssueRepositoryPort, times(1)).saveAll(issues);
        verify(scanRepositoryPort, times(1)).markCompleted(eq(scanId), any(LocalDateTime.class));
        verify(scanRepositoryPort, never()).markFailed(eq(scanId), any(LocalDateTime.class), anyString());
    }

    @Test
    void shouldMarkScanFailedWhenScannerThrowsException() {
        Long scanId = 11L;
        Long projectId = 3L;
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(3);

        Project project = new Project(projectId, "Accessibility Copilot", "https://broken.example.com", LocalDateTime.now());
        Scan runningScan = new Scan(scanId, projectId, ScanStatus.RUNNING, startedAt, null, null);

        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.of(project));
        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(runningScan));
        when(webAccessibilityScannerPort.scan(scanId, project.getRootUrl()))
                .thenThrow(new IllegalStateException("Navigation timeout"));
        when(scanRepositoryPort.markFailed(eq(scanId), any(LocalDateTime.class), anyString()))
                .thenReturn(true);

        scanExecutionService.executeAsync(scanId, projectId);

        verify(projectRepositoryPort, times(1)).findById(projectId);
        verify(scanRepositoryPort, times(2)).findById(scanId);
        verify(webAccessibilityScannerPort, times(1)).scan(scanId, project.getRootUrl());
        verify(accessibilityIssueRepositoryPort, never()).saveAll(anyList());
        verify(scanRepositoryPort, never()).markCompleted(eq(scanId), any(LocalDateTime.class));

        ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(scanRepositoryPort, times(1))
                .markFailed(eq(scanId), any(LocalDateTime.class), errorMessageCaptor.capture());
        assertEquals("Navigation timeout", errorMessageCaptor.getValue());
    }

    @Test
    void shouldSkipCompletionTransitionWhenScanIsNotRunning() {
        Long scanId = 12L;
        Long projectId = 4L;
        Scan completedScan = new Scan(scanId, projectId, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now(), null);

        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(completedScan));

        scanExecutionService.executeAsync(scanId, projectId);

        verify(scanRepositoryPort, times(1)).findById(scanId);
        verify(projectRepositoryPort, never()).findById(projectId);
        verify(webAccessibilityScannerPort, never()).scan(eq(scanId), anyString());
        verify(scanRepositoryPort, never()).markCompleted(eq(scanId), any(LocalDateTime.class));
        verify(scanRepositoryPort, never()).markFailed(eq(scanId), any(LocalDateTime.class), anyString());
    }
}



