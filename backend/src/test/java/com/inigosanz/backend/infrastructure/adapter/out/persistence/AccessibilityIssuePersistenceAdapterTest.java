package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessibilityIssuePersistenceAdapterTest {

    @Mock
    private JpaAccessibilityIssueRepository jpaAccessibilityIssueRepository;

    @Mock
    private AccessibilityIssuePersistenceMapper accessibilityIssuePersistenceMapper;

    @InjectMocks
    private AccessibilityIssuePersistenceAdapter accessibilityIssuePersistenceAdapter;

    @Test
    void shouldUseStableOrderedQueryWhenFindingByScanId() {
        Long scanId = 10L;

        AccessibilityIssueEntity firstEntity = new AccessibilityIssueEntity();
        AccessibilityIssueEntity secondEntity = new AccessibilityIssueEntity();

        List<AccessibilityIssue> mappedIssues = List.of(
                mock(AccessibilityIssue.class),
                mock(AccessibilityIssue.class)
        );

        List<AccessibilityIssueEntity> entities = List.of(firstEntity, secondEntity);

        when(jpaAccessibilityIssueRepository.findByScanIdOrderByIdAsc(scanId)).thenReturn(entities);
        when(accessibilityIssuePersistenceMapper.toDomainList(entities)).thenReturn(mappedIssues);

        List<AccessibilityIssue> result = accessibilityIssuePersistenceAdapter.findByScanId(scanId);

        verify(jpaAccessibilityIssueRepository, times(1)).findByScanIdOrderByIdAsc(scanId);
        verify(accessibilityIssuePersistenceMapper, times(1)).toDomainList(entities);
        assertSame(mappedIssues, result);
    }
}


