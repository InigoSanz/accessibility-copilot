package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.domain.port.in.GetProjectByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListProjectsUseCase;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProjectService implements CreateProjectUseCase, ListProjectsUseCase, GetProjectByIdUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;

    public ProjectService(ProjectRepositoryPort projectRepositoryPort) {
        this.projectRepositoryPort = projectRepositoryPort;
    }

    @Override
    public Project create(Project project) {
        Objects.requireNonNull(project, "project is required");
        return projectRepositoryPort.save(project);
    }

    @Override
    public List<Project> findAll() {
        return projectRepositoryPort.findAll();
    }

    @Override
    public Project findById(Long id) {
        Objects.requireNonNull(id, "id is required");
        return projectRepositoryPort.findById(id)
                .orElseThrow(ProjectNotFoundException::new);
    }
}
