package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaScanRepository extends JpaRepository<ScanEntity, Long> {

    List<ScanEntity> findByProjectId(Long projectId);
}
