package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProjectService implements CreateProjectUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;

    public ProjectService(ProjectRepositoryPort projectRepositoryPort) {
        this.projectRepositoryPort = projectRepositoryPort;
    }

    @Override
    public Project create(Project project) {
        Objects.requireNonNull(project, "project is required");
        return projectRepositoryPort.save(project);
    }
}

