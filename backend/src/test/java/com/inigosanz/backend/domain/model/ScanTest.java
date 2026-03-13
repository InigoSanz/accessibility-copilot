package com.inigosanz.backend.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScanTest {

    @Test
    void shouldAllowNullFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 13, 11, 0, 0);

        Scan scan = new Scan(1L, 10L, ScanStatus.RUNNING, startedAt, null);

        assertEquals(1L, scan.getId());
        assertEquals(10L, scan.getProjectId());
        assertEquals(ScanStatus.RUNNING, scan.getStatus());
        assertEquals(startedAt, scan.getStartedAt());
        assertNull(scan.getFinishedAt());
    }

    @Test
    void shouldRequireStartedAt() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Scan(1L, 10L, ScanStatus.PENDING, null, null)
        );

        assertEquals("startedAt is required", exception.getMessage());
    }
}

