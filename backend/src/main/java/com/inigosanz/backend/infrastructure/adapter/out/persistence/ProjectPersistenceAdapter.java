package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Project;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final JpaProjectRepository jpaProjectRepository;
    private final ProjectPersistenceMapper projectPersistenceMapper;

    public ProjectPersistenceAdapter(
            JpaProjectRepository jpaProjectRepository,
            ProjectPersistenceMapper projectPersistenceMapper
    ) {
        this.jpaProjectRepository = jpaProjectRepository;
        this.projectPersistenceMapper = projectPersistenceMapper;
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entityToSave = projectPersistenceMapper.toEntity(project);
        ProjectEntity savedEntity = jpaProjectRepository.save(entityToSave);
        return projectPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public List<Project> findAll() {
        return jpaProjectRepository.findAll()
                .stream()
                .map(projectPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Project> findById(Long id) {
        return jpaProjectRepository.findById(id)
                .map(projectPersistenceMapper::toDomain);
    }
}
