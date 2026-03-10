package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepositoryPort projectRepositoryPort;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void create_shouldCallRepositorySaveAndReturnSavedProject() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 10, 10, 0);
        Project projectToCreate = new Project(null, "Mi proyecto", "https://example.com", createdAt);
        Project savedProject = new Project(1L, "Mi proyecto", "https://example.com", createdAt);

        when(projectRepositoryPort.save(projectToCreate)).thenReturn(savedProject);

        Project result = projectService.create(projectToCreate);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepositoryPort).save(projectCaptor.capture());

        Project capturedProject = projectCaptor.getValue();
        assertEquals(projectToCreate.getId(), capturedProject.getId());
        assertEquals(projectToCreate.getName(), capturedProject.getName());
        assertEquals(projectToCreate.getRootUrl(), capturedProject.getRootUrl());
        assertEquals(projectToCreate.getCreatedAt(), capturedProject.getCreatedAt());
        assertSame(savedProject, result);
    }
}

