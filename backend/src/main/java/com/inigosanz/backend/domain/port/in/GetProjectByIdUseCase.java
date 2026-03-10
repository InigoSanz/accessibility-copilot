package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.Project;

public interface GetProjectByIdUseCase {

    Project findById(Long id);
}

