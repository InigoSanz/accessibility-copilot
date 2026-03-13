package com.inigosanz.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Scan {

    private final Long id;
    private final Long projectId;
    private final ScanStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final String errorMessage;

    public Scan(
            Long id,
            Long projectId,
            ScanStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String errorMessage
    ) {
        this.id = id;
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt is required");
        this.finishedAt = finishedAt;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

