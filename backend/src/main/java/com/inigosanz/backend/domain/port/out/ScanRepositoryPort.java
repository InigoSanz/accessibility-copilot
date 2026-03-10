package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Scan;

import java.util.List;
import java.util.Optional;

public interface ScanRepositoryPort {

    Scan save(Scan scan);

    List<Scan> findByProjectId(Long projectId);

    Optional<Scan> findById(Long scanId);
}
