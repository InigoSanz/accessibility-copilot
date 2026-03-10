package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepositoryPort {

    Project save(Project project);

    List<Project> findAll();

    Optional<Project> findById(Long id);
}
