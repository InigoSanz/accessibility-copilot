package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Project;

public interface ProjectRepositoryPort {

    Project save(Project project);
}

