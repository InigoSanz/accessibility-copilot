package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.domain.port.in.GetScanByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListScansByProjectUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanResponse;
import com.inigosanz.backend.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
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
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private ListScansByProjectUseCase listScansByProjectUseCase;

    @Autowired
    private GetScanByIdUseCase getScanByIdUseCase;

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

    @Test
    void findByProjectId_shouldReturnScanListJson() throws Exception {
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 10, 12, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 3, 10, 12, 0, 1);

        Scan scan = new Scan(1L, 1L, ScanStatus.COMPLETED, startedAt, finishedAt);
        ScanResponse response = new ScanResponse(1L, 1L, "COMPLETED", startedAt, finishedAt);

        when(listScansByProjectUseCase.findByProjectId(1L)).thenReturn(List.of(scan));
        when(scanWebMapper.toResponseList(List.of(scan))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects/1/scans"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].startedAt").value("2026-03-10T12:00:00"))
                .andExpect(jsonPath("$[0].finishedAt").value("2026-03-10T12:00:01"));

        verify(listScansByProjectUseCase).findByProjectId(1L);
        verify(scanWebMapper).toResponseList(List.of(scan));
    }

    @Test
    void findById_shouldReturnScanJson() throws Exception {
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 10, 12, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 3, 10, 12, 0, 1);

        Scan scan = new Scan(1L, 1L, ScanStatus.COMPLETED, startedAt, finishedAt);
        ScanResponse response = new ScanResponse(1L, 1L, "COMPLETED", startedAt, finishedAt);

        when(getScanByIdUseCase.findById(1L)).thenReturn(scan);
        when(scanWebMapper.toResponse(scan)).thenReturn(response);

        mockMvc.perform(get("/api/scans/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.startedAt").value("2026-03-10T12:00:00"))
                .andExpect(jsonPath("$.finishedAt").value("2026-03-10T12:00:01"));

        verify(getScanByIdUseCase).findById(1L);
        verify(scanWebMapper).toResponse(scan);
    }

    @Test
    void findById_shouldReturnScanJsonWithNullFinishedAt() throws Exception {
        LocalDateTime startedAt = LocalDateTime.of(2026, 3, 10, 12, 0, 0);

        Scan scan = new Scan(2L, 1L, ScanStatus.RUNNING, startedAt, null);
        ScanResponse response = new ScanResponse(2L, 1L, "RUNNING", startedAt, null);

        when(getScanByIdUseCase.findById(2L)).thenReturn(scan);
        when(scanWebMapper.toResponse(scan)).thenReturn(response);

        mockMvc.perform(get("/api/scans/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.startedAt").value("2026-03-10T12:00:00"))
                .andExpect(jsonPath("$.finishedAt").value(nullValue()));

        verify(getScanByIdUseCase).findById(2L);
        verify(scanWebMapper).toResponse(scan);
    }

    @Test
    void findById_whenScanDoesNotExist_shouldReturnNotFound() throws Exception {
        when(getScanByIdUseCase.findById(999L)).thenThrow(new ScanNotFoundException());

        mockMvc.perform(get("/api/scans/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Scan not found"));

        verify(getScanByIdUseCase).findById(999L);
    }

    @Configuration
    static class MockConfig {

        @Bean
        CreateScanUseCase createScanUseCase() {
            return mock(CreateScanUseCase.class);
        }

        @Bean
        ListScansByProjectUseCase listScansByProjectUseCase() {
            return mock(ListScansByProjectUseCase.class);
        }

        @Bean
        GetScanByIdUseCase getScanByIdUseCase() {
            return mock(GetScanByIdUseCase.class);
        }

        @Bean
        ScanWebMapper scanWebMapper() {
            return mock(ScanWebMapper.class);
        }
    }
}

