package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccessibilityIssuePersistenceMapper {

    AccessibilityIssue toDomain(AccessibilityIssueEntity entity);

    AccessibilityIssueEntity toEntity(AccessibilityIssue issue);

    List<AccessibilityIssue> toDomainList(List<AccessibilityIssueEntity> entities);

    List<AccessibilityIssueEntity> toEntityList(List<AccessibilityIssue> issues);
}

