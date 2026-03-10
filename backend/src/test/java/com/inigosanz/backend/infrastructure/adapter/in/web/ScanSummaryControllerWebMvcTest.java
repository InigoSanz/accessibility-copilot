package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.ScanSummary;
import com.inigosanz.backend.domain.port.in.GetScanSummaryUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanSummaryResponse;
import com.inigosanz.backend.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
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

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScanSummaryController.class)
@Import({ScanSummaryController.class, GlobalExceptionHandler.class, ScanSummaryControllerWebMvcTest.MockConfig.class})
@ContextConfiguration(classes = {ScanSummaryController.class, GlobalExceptionHandler.class, ScanSummaryControllerWebMvcTest.MockConfig.class})
class ScanSummaryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GetScanSummaryUseCase getScanSummaryUseCase;

    @Autowired
    private ScanSummaryWebMapper scanSummaryWebMapper;

    @Test
    void getSummary_shouldReturnSummaryJson() throws Exception {
        Long scanId = 1L;
        ScanSummary summary = new ScanSummary(scanId, 3L, Map.of("serious", 2L, "moderate", 1L));
        ScanSummaryResponse response = new ScanSummaryResponse(scanId, 3L, Map.of("serious", 2L, "moderate", 1L));

        when(getScanSummaryUseCase.getSummary(scanId)).thenReturn(summary);
        when(scanSummaryWebMapper.toResponse(summary)).thenReturn(response);

        mockMvc.perform(get("/api/scans/1/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scanId").value(1))
                .andExpect(jsonPath("$.totalIssues").value(3))
                .andExpect(jsonPath("$.bySeverity.serious").value(2))
                .andExpect(jsonPath("$.bySeverity.moderate").value(1));

        verify(getScanSummaryUseCase).getSummary(scanId);
        verify(scanSummaryWebMapper).toResponse(summary);
    }

    @Test
    void getSummary_whenScanDoesNotExist_shouldReturnNotFound() throws Exception {
        when(getScanSummaryUseCase.getSummary(999L)).thenThrow(new ScanNotFoundException());

        mockMvc.perform(get("/api/scans/999/summary"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Scan not found"));

        verify(getScanSummaryUseCase).getSummary(999L);
    }

    @Configuration
    static class MockConfig {

        @Bean
        GetScanSummaryUseCase getScanSummaryUseCase() {
            return mock(GetScanSummaryUseCase.class);
        }

        @Bean
        ScanSummaryWebMapper scanSummaryWebMapper() {
            return mock(ScanSummaryWebMapper.class);
        }
    }
}

