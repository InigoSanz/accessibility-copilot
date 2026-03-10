package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Project;

import java.util.List;

public interface ProjectRepositoryPort {

    Project save(Project project);

    List<Project> findAll();
}
