package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanPersistenceAdapterTest {

    @Mock
    private JpaScanRepository jpaScanRepository;

    @Mock
    private ScanPersistenceMapper scanPersistenceMapper;

    @InjectMocks
    private ScanPersistenceAdapter scanPersistenceAdapter;

    @Test
    void shouldUseOrderedQueryWhenFindingByProjectId() {
        Long projectId = 1L;

        ScanEntity firstEntity = new ScanEntity();
        ScanEntity secondEntity = new ScanEntity();

        Scan firstScan = new Scan(2L, projectId, ScanStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now(), null);
        Scan secondScan = new Scan(1L, projectId, ScanStatus.RUNNING, LocalDateTime.now().minusMinutes(1), null, null);

        when(jpaScanRepository.findByProjectIdOrderByStartedAtDescIdDesc(projectId))
                .thenReturn(List.of(firstEntity, secondEntity));
        when(scanPersistenceMapper.toDomain(firstEntity)).thenReturn(firstScan);
        when(scanPersistenceMapper.toDomain(secondEntity)).thenReturn(secondScan);

        List<Scan> result = scanPersistenceAdapter.findByProjectId(projectId);

        verify(jpaScanRepository, times(1)).findByProjectIdOrderByStartedAtDescIdDesc(projectId);
        verify(scanPersistenceMapper, times(1)).toDomain(firstEntity);
        verify(scanPersistenceMapper, times(1)).toDomain(secondEntity);

        assertEquals(2, result.size());
        assertSame(firstScan, result.get(0));
        assertSame(secondScan, result.get(1));
    }

    @Test
    void shouldMarkCompletedOnlyWhenCurrentStatusIsRunning() {
        Long scanId = 20L;
        LocalDateTime finishedAt = LocalDateTime.now();

        when(jpaScanRepository.updateStatusIfCurrentStatus(
                scanId,
                ScanStatus.RUNNING,
                ScanStatus.COMPLETED,
                finishedAt,
                null
        )).thenReturn(1);

        boolean updated = scanPersistenceAdapter.markCompleted(scanId, finishedAt);

        verify(jpaScanRepository, times(1)).updateStatusIfCurrentStatus(
                scanId,
                ScanStatus.RUNNING,
                ScanStatus.COMPLETED,
                finishedAt,
                null
        );
        assertTrue(updated);
    }

    @Test
    void shouldMarkFailedOnlyWhenCurrentStatusIsRunning() {
        Long scanId = 21L;
        LocalDateTime finishedAt = LocalDateTime.now();
        String errorMessage = "Navigation timeout";

        when(jpaScanRepository.updateStatusIfCurrentStatus(
                scanId,
                ScanStatus.RUNNING,
                ScanStatus.FAILED,
                finishedAt,
                errorMessage
        )).thenReturn(0);

        boolean updated = scanPersistenceAdapter.markFailed(scanId, finishedAt, errorMessage);

        verify(jpaScanRepository, times(1)).updateStatusIfCurrentStatus(
                scanId,
                ScanStatus.RUNNING,
                ScanStatus.FAILED,
                finishedAt,
                errorMessage
        );
        assertFalse(updated);
    }
}

