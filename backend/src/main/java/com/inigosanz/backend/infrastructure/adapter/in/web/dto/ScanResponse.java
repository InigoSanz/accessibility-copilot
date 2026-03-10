package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ScanResponse(
        Long id,
        Long projectId,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}

