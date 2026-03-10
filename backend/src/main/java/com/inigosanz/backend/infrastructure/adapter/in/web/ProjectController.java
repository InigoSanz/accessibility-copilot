package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.in.CreateProjectUseCase;
import com.inigosanz.backend.domain.port.in.GetProjectByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListProjectsUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.CreateProjectRequest;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final ListProjectsUseCase listProjectsUseCase;
    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final ProjectWebMapper projectWebMapper;

    public ProjectController(
            CreateProjectUseCase createProjectUseCase,
            ListProjectsUseCase listProjectsUseCase,
            GetProjectByIdUseCase getProjectByIdUseCase,
            ProjectWebMapper projectWebMapper
    ) {
        this.createProjectUseCase = createProjectUseCase;
        this.listProjectsUseCase = listProjectsUseCase;
        this.getProjectByIdUseCase = getProjectByIdUseCase;
        this.projectWebMapper = projectWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        Project projectToCreate = projectWebMapper.toDomain(request);
        Project createdProject = createProjectUseCase.create(projectToCreate);
        return projectWebMapper.toResponse(createdProject);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll() {
        List<Project> projects = listProjectsUseCase.findAll();
        List<ProjectResponse> response = projectWebMapper.toResponseList(projects);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id) {
        Project project = getProjectByIdUseCase.findById(id);
        ProjectResponse response = projectWebMapper.toResponse(project);
        return ResponseEntity.ok(response);
    }
}
