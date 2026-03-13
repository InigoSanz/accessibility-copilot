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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
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

        scanExecutionService.executeAsync(scanId, projectId);

        verify(projectRepositoryPort, times(1)).findById(projectId);
        verify(scanRepositoryPort, times(1)).findById(scanId);
        verify(webAccessibilityScannerPort, times(1)).scan(scanId, project.getRootUrl());
        verify(accessibilityIssueRepositoryPort, times(1)).saveAll(issues);

        ArgumentCaptor<Scan> completedCaptor = ArgumentCaptor.forClass(Scan.class);
        verify(scanRepositoryPort, times(1)).save(completedCaptor.capture());

        Scan completedScan = completedCaptor.getValue();
        assertEquals(scanId, completedScan.getId());
        assertEquals(projectId, completedScan.getProjectId());
        assertEquals(ScanStatus.COMPLETED, completedScan.getStatus());
        assertEquals(startedAt, completedScan.getStartedAt());
        assertNotNull(completedScan.getFinishedAt());
        assertNull(completedScan.getErrorMessage());
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

        scanExecutionService.executeAsync(scanId, projectId);

        verify(projectRepositoryPort, times(1)).findById(projectId);
        verify(scanRepositoryPort, times(2)).findById(scanId);
        verify(webAccessibilityScannerPort, times(1)).scan(scanId, project.getRootUrl());
        verify(accessibilityIssueRepositoryPort, never()).saveAll(anyList());

        ArgumentCaptor<Scan> failedCaptor = ArgumentCaptor.forClass(Scan.class);
        verify(scanRepositoryPort, times(1)).save(failedCaptor.capture());

        Scan failedScan = failedCaptor.getValue();
        assertEquals(scanId, failedScan.getId());
        assertEquals(projectId, failedScan.getProjectId());
        assertEquals(ScanStatus.FAILED, failedScan.getStatus());
        assertEquals(startedAt, failedScan.getStartedAt());
        assertNotNull(failedScan.getFinishedAt());
        assertEquals("Navigation timeout", failedScan.getErrorMessage());
    }
}



