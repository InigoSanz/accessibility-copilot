package com.inigosanz.backend.infrastructure.adapter.in.web.dto;

import java.util.Map;

public record ValidationErrorResponse(
        String message,
        Map<String, String> errors
) {
}

