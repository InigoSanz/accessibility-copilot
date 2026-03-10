package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.domain.port.in.GetProjectByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListProjectsUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({ProjectController.class, GlobalExceptionHandler.class, ProjectControllerWebMvcTest.MockConfig.class})
@ContextConfiguration(classes = {ProjectController.class, GlobalExceptionHandler.class, ProjectControllerWebMvcTest.MockConfig.class})
class ProjectControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreateProjectUseCase createProjectUseCase;

    @Autowired
    private ListProjectsUseCase listProjectsUseCase;

    @Autowired
    private GetProjectByIdUseCase getProjectByIdUseCase;

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

    @Test
    void list_shouldReturnProjectsJson() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 10, 10, 45, 13, 750_659_300);
        Project domainProject = new Project(1L, "Accessibility Copilot", "https://example.com", createdAt);
        ProjectResponse response = new ProjectResponse(1L, "Accessibility Copilot", "https://example.com", createdAt);

        when(listProjectsUseCase.findAll()).thenReturn(List.of(domainProject));
        when(projectWebMapper.toResponseList(List.of(domainProject))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Accessibility Copilot"))
                .andExpect(jsonPath("$[0].rootUrl").value("https://example.com"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-03-10T10:45:13.7506593"));

        verify(listProjectsUseCase).findAll();
        verify(projectWebMapper).toResponseList(List.of(domainProject));
    }

    @Test
    void findById_whenProjectExists_shouldReturnProjectJson() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 10, 10, 45, 13, 750_659_300);
        Project project = new Project(1L, "Accessibility Copilot", "https://example.com", createdAt);
        ProjectResponse response = new ProjectResponse(1L, "Accessibility Copilot", "https://example.com", createdAt);

        when(getProjectByIdUseCase.findById(1L)).thenReturn(project);
        when(projectWebMapper.toResponse(project)).thenReturn(response);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Accessibility Copilot"))
                .andExpect(jsonPath("$.rootUrl").value("https://example.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-03-10T10:45:13.7506593"));

        verify(getProjectByIdUseCase).findById(1L);
        verify(projectWebMapper).toResponse(project);
    }

    @Test
    void findById_whenProjectDoesNotExist_shouldReturnNotFound() throws Exception {
        when(getProjectByIdUseCase.findById(999L)).thenThrow(new ProjectNotFoundException());

        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Project not found"));

        verify(getProjectByIdUseCase).findById(999L);
    }

    @Configuration
    static class MockConfig {

        @Bean
        CreateProjectUseCase createProjectUseCase() {
            return mock(CreateProjectUseCase.class);
        }

        @Bean
        ListProjectsUseCase listProjectsUseCase() {
            return mock(ListProjectsUseCase.class);
        }

        @Bean
        GetProjectByIdUseCase getProjectByIdUseCase() {
            return mock(GetProjectByIdUseCase.class);
        }

        @Bean
        ProjectWebMapper projectWebMapper() {
            return mock(ProjectWebMapper.class);
        }
    }
}
