package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.port.in.ListIssuesByScanUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.AccessibilityIssueResponse;
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

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessibilityIssueController.class)
@Import({AccessibilityIssueController.class, GlobalExceptionHandler.class, AccessibilityIssueControllerWebMvcTest.MockConfig.class})
@ContextConfiguration(classes = {AccessibilityIssueController.class, GlobalExceptionHandler.class, AccessibilityIssueControllerWebMvcTest.MockConfig.class})
class AccessibilityIssueControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListIssuesByScanUseCase listIssuesByScanUseCase;

    @Autowired
    private AccessibilityIssueWebMapper accessibilityIssueWebMapper;

    @Test
    void findByScanId_shouldReturnIssuesListJson() throws Exception {
        Long scanId = 1L;

        AccessibilityIssue issue = new AccessibilityIssue(
                1L,
                scanId,
                "WCAG_1_1_1",
                "Image without alt text",
                "HIGH",
                "1.1.1",
                "https://example.com",
                "<img src=\"banner.jpg\">",
                "img.hero-banner",
                "Add a meaningful alt attribute describing the image purpose.",
                "https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html"
        );

        AccessibilityIssueResponse response = new AccessibilityIssueResponse(
                1L,
                scanId,
                "WCAG_1_1_1",
                "Image without alt text",
                "HIGH",
                "1.1.1",
                "https://example.com",
                "<img src=\"banner.jpg\">",
                "img.hero-banner",
                "Add a meaningful alt attribute describing the image purpose.",
                "https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html"
        );

        when(listIssuesByScanUseCase.findByScanId(scanId)).thenReturn(List.of(issue));
        when(accessibilityIssueWebMapper.toResponseList(List.of(issue))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/scans/1/issues"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].scanId").value(1))
                .andExpect(jsonPath("$[0].ruleCode").value("WCAG_1_1_1"))
                .andExpect(jsonPath("$[0].message").value("Image without alt text"))
                .andExpect(jsonPath("$[0].severity").value("HIGH"))
                .andExpect(jsonPath("$[0].wcagCriterion").value("1.1.1"))
                .andExpect(jsonPath("$[0].pageUrl").value("https://example.com"))
                .andExpect(jsonPath("$[0].htmlSnippet").value("<img src=\"banner.jpg\">"))
                .andExpect(jsonPath("$[0].selector").value("img.hero-banner"))
                .andExpect(jsonPath("$[0].recommendation").value("Add a meaningful alt attribute describing the image purpose."))
                .andExpect(jsonPath("$[0].helpUrl").value("https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html"));

        verify(listIssuesByScanUseCase).findByScanId(scanId);
        verify(accessibilityIssueWebMapper).toResponseList(List.of(issue));
    }

    @Test
    void findByScanId_whenScanDoesNotExist_shouldReturnNotFound() throws Exception {
        when(listIssuesByScanUseCase.findByScanId(999L)).thenThrow(new ScanNotFoundException());

        mockMvc.perform(get("/api/scans/999/issues"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Scan not found"));

        verify(listIssuesByScanUseCase).findByScanId(999L);
    }

    @Configuration
    static class MockConfig {

        @Bean
        ListIssuesByScanUseCase listIssuesByScanUseCase() {
            return mock(ListIssuesByScanUseCase.class);
        }

        @Bean
        AccessibilityIssueWebMapper accessibilityIssueWebMapper() {
            return mock(AccessibilityIssueWebMapper.class);
        }
    }
}

