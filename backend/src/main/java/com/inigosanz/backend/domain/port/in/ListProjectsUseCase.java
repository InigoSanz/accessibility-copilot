package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.Project;

import java.util.List;

public interface ListProjectsUseCase {

    List<Project> list();
}

