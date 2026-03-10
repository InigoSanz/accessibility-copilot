package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.Project;

public interface CreateProjectUseCase {

    Project create(Project project);
}

