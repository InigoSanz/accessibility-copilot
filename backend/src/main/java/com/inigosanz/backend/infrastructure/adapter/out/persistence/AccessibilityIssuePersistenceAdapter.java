package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessibilityIssuePersistenceAdapter implements AccessibilityIssueRepositoryPort {

    private final JpaAccessibilityIssueRepository jpaAccessibilityIssueRepository;
    private final AccessibilityIssuePersistenceMapper accessibilityIssuePersistenceMapper;

    public AccessibilityIssuePersistenceAdapter(
            JpaAccessibilityIssueRepository jpaAccessibilityIssueRepository,
            AccessibilityIssuePersistenceMapper accessibilityIssuePersistenceMapper
    ) {
        this.jpaAccessibilityIssueRepository = jpaAccessibilityIssueRepository;
        this.accessibilityIssuePersistenceMapper = accessibilityIssuePersistenceMapper;
    }

    @Override
    public List<AccessibilityIssue> findByScanId(Long scanId) {
        List<AccessibilityIssueEntity> entities = jpaAccessibilityIssueRepository.findByScanIdOrderByIdAsc(scanId);
        return accessibilityIssuePersistenceMapper.toDomainList(entities);
    }

    @Override
    public List<AccessibilityIssue> saveAll(List<AccessibilityIssue> issues) {
        List<AccessibilityIssueEntity> entitiesToSave = accessibilityIssuePersistenceMapper.toEntityList(issues);
        List<AccessibilityIssueEntity> savedEntities = jpaAccessibilityIssueRepository.saveAll(entitiesToSave);
        return accessibilityIssuePersistenceMapper.toDomainList(savedEntities);
    }
}

