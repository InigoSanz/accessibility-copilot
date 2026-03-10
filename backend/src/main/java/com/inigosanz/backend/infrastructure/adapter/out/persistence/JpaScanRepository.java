package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaScanRepository extends JpaRepository<ScanEntity, Long> {
}

