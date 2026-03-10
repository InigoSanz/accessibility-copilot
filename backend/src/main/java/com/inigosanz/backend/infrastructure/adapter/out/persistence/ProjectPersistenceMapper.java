package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectPersistenceMapper {

    ProjectEntity toEntity(Project project);

    Project toDomain(ProjectEntity projectEntity);
}

