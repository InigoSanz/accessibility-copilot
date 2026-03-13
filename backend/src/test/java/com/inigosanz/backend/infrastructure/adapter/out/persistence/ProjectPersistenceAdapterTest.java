package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPersistenceAdapterTest {

    @Mock
    private JpaProjectRepository jpaProjectRepository;

    @Mock
    private ProjectPersistenceMapper projectPersistenceMapper;

    @InjectMocks
    private ProjectPersistenceAdapter projectPersistenceAdapter;

    @Test
    void shouldUseOrderedQueryWhenFindingAllProjects() {
        ProjectEntity newestEntity = new ProjectEntity();
        ProjectEntity olderEntity = new ProjectEntity();

        Project newestProject = new Project(2L, "Nuevo", "https://new.example.com", LocalDateTime.now());
        Project olderProject = new Project(1L, "Antiguo", "https://old.example.com", LocalDateTime.now().minusDays(1));

        when(jpaProjectRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(newestEntity, olderEntity));
        when(projectPersistenceMapper.toDomain(newestEntity)).thenReturn(newestProject);
        when(projectPersistenceMapper.toDomain(olderEntity)).thenReturn(olderProject);

        List<Project> result = projectPersistenceAdapter.findAll();

        verify(jpaProjectRepository, times(1)).findAllByOrderByCreatedAtDescIdDesc();
        verify(projectPersistenceMapper, times(1)).toDomain(newestEntity);
        verify(projectPersistenceMapper, times(1)).toDomain(olderEntity);

        assertEquals(2, result.size());
        assertSame(newestProject, result.get(0));
        assertSame(olderProject, result.get(1));
    }
}

