package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanResponse;
import com.inigosanz.backend.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScanController.class)
@Import({ScanController.class, GlobalExceptionHandler.class, ScanControllerWebMvcTest.MockConfig.class})
@ContextConfiguration(classes = {ScanController.class, GlobalExceptionHandler.class, ScanControllerWebMvcTest.MockConfig.class})
class ScanControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreateScanUseCase createScanUseCase;

    @Autowired
    private ScanWebMapper scanWebMapper;

    @Test
    void create_whenProjectExists_shouldReturnCreatedScan() throws Exception {
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 10, 14, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 3, 10, 14, 0, 1);

        Scan scan = new Scan(1L, 1L, ScanStatus.COMPLETED, startedAt, finishedAt);
        ScanResponse response = new ScanResponse(1L, 1L, "COMPLETED", startedAt, finishedAt);

        when(createScanUseCase.create(1L)).thenReturn(scan);
        when(scanWebMapper.toResponse(scan)).thenReturn(response);

        mockMvc.perform(post("/api/projects/1/scans"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.startedAt").value("2026-03-10T14:00:00"))
                .andExpect(jsonPath("$.finishedAt").value("2026-03-10T14:00:01"));

        verify(createScanUseCase).create(1L);
        verify(scanWebMapper).toResponse(scan);
    }

    @Test
    void create_whenProjectDoesNotExist_shouldReturnNotFound() throws Exception {
        when(createScanUseCase.create(999L)).thenThrow(new ProjectNotFoundException());

        mockMvc.perform(post("/api/projects/999/scans"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project not found"));

        verify(createScanUseCase).create(999L);
    }

    @Configuration
    static class MockConfig {

        @Bean
        CreateScanUseCase createScanUseCase() {
            return mock(CreateScanUseCase.class);
        }

        @Bean
        ScanWebMapper scanWebMapper() {
            return mock(ScanWebMapper.class);
        }
    }
}

