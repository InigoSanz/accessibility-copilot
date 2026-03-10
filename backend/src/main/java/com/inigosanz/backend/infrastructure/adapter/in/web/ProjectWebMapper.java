package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.CreateProjectRequest;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class ProjectWebMapper {

    public Project toDomain(CreateProjectRequest request) {
        Objects.requireNonNull(request, "request is required");
        return new Project(null, request.name(), request.rootUrl(), LocalDateTime.now());
    }

    public ProjectResponse toResponse(Project project) {
        Objects.requireNonNull(project, "project is required");
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getRootUrl(),
                project.getCreatedAt()
        );
    }
}

