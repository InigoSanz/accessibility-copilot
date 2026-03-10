package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.Scan;

import java.util.List;

public interface ListScansByProjectUseCase {

    List<Scan> findByProjectId(Long projectId);
}

