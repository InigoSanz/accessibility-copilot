package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ScanPersistenceAdapter implements ScanRepositoryPort {

    private final JpaScanRepository jpaScanRepository;
    private final ScanPersistenceMapper scanPersistenceMapper;

    public ScanPersistenceAdapter(JpaScanRepository jpaScanRepository, ScanPersistenceMapper scanPersistenceMapper) {
        this.jpaScanRepository = jpaScanRepository;
        this.scanPersistenceMapper = scanPersistenceMapper;
    }

    @Override
    public Scan save(Scan scan) {
        ScanEntity entityToSave = scanPersistenceMapper.toEntity(scan);
        ScanEntity savedEntity = jpaScanRepository.save(entityToSave);
        return scanPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public List<Scan> findByProjectId(Long projectId) {
        return jpaScanRepository.findByProjectIdOrderByStartedAtDescIdDesc(projectId)
                .stream()
                .map(scanPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Scan> findById(Long scanId) {
        return jpaScanRepository.findById(scanId)
                .map(scanPersistenceMapper::toDomain);
    }
}
