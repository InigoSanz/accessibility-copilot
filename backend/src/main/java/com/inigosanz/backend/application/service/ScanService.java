package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ScanService implements CreateScanUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ScanRepositoryPort scanRepositoryPort;

    public ScanService(ProjectRepositoryPort projectRepositoryPort, ScanRepositoryPort scanRepositoryPort) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.scanRepositoryPort = scanRepositoryPort;
    }

    @Override
    public Scan create(Long projectId) {
        Objects.requireNonNull(projectId, "projectId is required");

        projectRepositoryPort.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        Scan scan = new Scan(
                null,
                projectId,
                ScanStatus.COMPLETED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return scanRepositoryPort.save(scan);
    }
}

