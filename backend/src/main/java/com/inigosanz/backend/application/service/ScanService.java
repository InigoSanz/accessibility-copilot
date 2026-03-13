package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.domain.port.in.GetScanByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListScansByProjectUseCase;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ScanService implements CreateScanUseCase, ListScansByProjectUseCase, GetScanByIdUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ScanRepositoryPort scanRepositoryPort;
    private final ScanExecutionService scanExecutionService;

    public ScanService(
            ProjectRepositoryPort projectRepositoryPort,
            ScanRepositoryPort scanRepositoryPort,
            ScanExecutionService scanExecutionService
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.scanRepositoryPort = scanRepositoryPort;
        this.scanExecutionService = scanExecutionService;
    }

    @Override
    public Scan create(Long projectId) {
        Objects.requireNonNull(projectId, "projectId is required");

        projectRepositoryPort.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        LocalDateTime startedAt = LocalDateTime.now();
        Scan scan = new Scan(
                null,
                projectId,
                ScanStatus.RUNNING,
                startedAt,
                null,
                null
        );

        Scan savedScan = scanRepositoryPort.save(scan);
        scanExecutionService.executeAsync(savedScan.getId(), projectId);

        return savedScan;
    }

    @Override
    public List<Scan> findByProjectId(Long projectId) {
        Objects.requireNonNull(projectId, "projectId is required");

        projectRepositoryPort.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        return scanRepositoryPort.findByProjectId(projectId);
    }

    @Override
    public Scan findById(Long scanId) {
        Objects.requireNonNull(scanId, "scanId is required");

        return scanRepositoryPort.findById(scanId)
                .orElseThrow(ScanNotFoundException::new);
    }
}
