package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String rootUrl,
        LocalDateTime createdAt
) {
}

