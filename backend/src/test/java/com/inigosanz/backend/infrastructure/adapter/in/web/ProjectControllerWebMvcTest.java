package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({ProjectController.class, ProjectControllerWebMvcTest.MockConfig.class})
@ContextConfiguration(classes = {ProjectController.class, ProjectControllerWebMvcTest.MockConfig.class})
class ProjectControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreateProjectUseCase createProjectUseCase;

    @Autowired
    private ProjectWebMapper projectWebMapper;

    @Test
    void create_withValidRequest_shouldReturnCreatedProjectJson() throws Exception {
        String requestJson = """
                {
                  "name": "Mi proyecto",
                  "rootUrl": "https://example.com"
                }
                """;

        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 10, 11, 30, 0);
        Project projectToCreate = new Project(null, "Mi proyecto", "https://example.com", createdAt);
        Project createdProject = new Project(1L, "Mi proyecto", "https://example.com", createdAt);
        ProjectResponse response = new ProjectResponse(1L, "Mi proyecto", "https://example.com", createdAt);

        when(projectWebMapper.toDomain(any())).thenReturn(projectToCreate);
        when(createProjectUseCase.create(projectToCreate)).thenReturn(createdProject);
        when(projectWebMapper.toResponse(createdProject)).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mi proyecto"))
                .andExpect(jsonPath("$.rootUrl").value("https://example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-03-10T11:30:00"));

        verify(projectWebMapper).toDomain(any());
        verify(createProjectUseCase).create(projectToCreate);
        verify(projectWebMapper).toResponse(createdProject);
    }

    @Configuration
    static class MockConfig {

        @Bean
        CreateProjectUseCase createProjectUseCase() {
            return mock(CreateProjectUseCase.class);
        }

        @Bean
        ProjectWebMapper projectWebMapper() {
            return mock(ProjectWebMapper.class);
        }
    }
}
