package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank String name,
        @NotBlank String rootUrl
) {
}
