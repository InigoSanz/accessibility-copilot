package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

public record CreateProjectRequest(
        String name,
        String rootUrl
) {
}

