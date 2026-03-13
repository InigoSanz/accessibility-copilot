package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Scan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScanRepositoryPort {

    Scan save(Scan scan);

    boolean markCompleted(Long scanId, LocalDateTime finishedAt);

    boolean markFailed(Long scanId, LocalDateTime finishedAt, String errorMessage);

    List<Scan> findByProjectId(Long projectId);

    Optional<Scan> findById(Long scanId);
}
