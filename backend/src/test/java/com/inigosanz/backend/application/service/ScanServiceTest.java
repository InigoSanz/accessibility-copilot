package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private ProjectRepositoryPort projectRepositoryPort;

    @Mock
    private ScanRepositoryPort scanRepositoryPort;

    @InjectMocks
    private ScanService scanService;

    @Test
    void shouldCreateScanWhenProjectExists() {
        Long projectId = 1L;
        Project project = new Project(projectId, "Accessibility Copilot", "https://example.com", LocalDateTime.now());

        Scan savedScan = new Scan(
                10L,
                projectId,
                ScanStatus.COMPLETED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.of(project));
        when(scanRepositoryPort.save(any(Scan.class))).thenReturn(savedScan);

        Scan result = scanService.create(projectId);

        ArgumentCaptor<Scan> scanCaptor = ArgumentCaptor.forClass(Scan.class);
        verify(scanRepositoryPort, times(1)).save(scanCaptor.capture());
        verify(projectRepositoryPort, times(1)).findById(projectId);

        Scan capturedScan = scanCaptor.getValue();
        assertEquals(projectId, capturedScan.getProjectId());
        assertEquals(ScanStatus.COMPLETED, capturedScan.getStatus());
        assertNotNull(capturedScan.getStartedAt());
        assertNotNull(capturedScan.getFinishedAt());
        assertSame(savedScan, result);
    }

    @Test
    void shouldThrowProjectNotFoundWhenProjectDoesNotExist() {
        Long projectId = 999L;
        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.empty());

        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class,
                () -> scanService.create(projectId)
        );

        verify(projectRepositoryPort, times(1)).findById(projectId);
        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void shouldListScansByProjectWhenProjectExists() {
        Long projectId = 1L;
        Project project = new Project(projectId, "Accessibility Copilot", "https://example.com", LocalDateTime.now());
        Scan scan = new Scan(1L, projectId, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.of(project));
        when(scanRepositoryPort.findByProjectId(projectId)).thenReturn(List.of(scan));

        List<Scan> result = scanService.findByProjectId(projectId);

        verify(projectRepositoryPort, times(1)).findById(projectId);
        verify(scanRepositoryPort, times(1)).findByProjectId(projectId);
        assertEquals(1, result.size());
        assertSame(scan, result.get(0));
    }

    @Test
    void shouldThrowProjectNotFoundWhenListingScansAndProjectDoesNotExist() {
        Long projectId = 999L;
        when(projectRepositoryPort.findById(projectId)).thenReturn(Optional.empty());

        ProjectNotFoundException exception = assertThrows(
                ProjectNotFoundException.class,
                () -> scanService.findByProjectId(projectId)
        );

        verify(projectRepositoryPort, times(1)).findById(projectId);
        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void shouldReturnScanByIdWhenExists() {
        Long scanId = 1L;
        Scan scan = new Scan(scanId, 1L, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());

        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.of(scan));

        Scan result = scanService.findById(scanId);

        verify(scanRepositoryPort, times(1)).findById(scanId);
        assertSame(scan, result);
    }

    @Test
    void shouldThrowScanNotFoundWhenScanDoesNotExist() {
        Long scanId = 999L;
        when(scanRepositoryPort.findById(scanId)).thenReturn(Optional.empty());

        ScanNotFoundException exception = assertThrows(
                ScanNotFoundException.class,
                () -> scanService.findById(scanId)
        );

        verify(scanRepositoryPort, times(1)).findById(scanId);
        assertEquals("Scan not found", exception.getMessage());
    }
}
