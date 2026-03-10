package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.CreateProjectRequest;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ProjectResponse;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ProjectWebMapper {

    default Project toDomain(CreateProjectRequest request) {
        Objects.requireNonNull(request, "request is required");
        return new Project(null, request.name(), request.rootUrl(), LocalDateTime.now());
    }

    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);
}
