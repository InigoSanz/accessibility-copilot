package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.CreateProjectRequest;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final ProjectWebMapper projectWebMapper;

    public ProjectController(CreateProjectUseCase createProjectUseCase, ProjectWebMapper projectWebMapper) {
        this.createProjectUseCase = createProjectUseCase;
        this.projectWebMapper = projectWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody CreateProjectRequest request) {
        Project projectToCreate = projectWebMapper.toDomain(request);
        Project createdProject = createProjectUseCase.create(projectToCreate);
        return projectWebMapper.toResponse(createdProject);
    }
}

