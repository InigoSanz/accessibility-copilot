package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAccessibilityIssueRepository extends JpaRepository<AccessibilityIssueEntity, Long> {

    List<AccessibilityIssueEntity> findByScanId(Long scanId);
}

