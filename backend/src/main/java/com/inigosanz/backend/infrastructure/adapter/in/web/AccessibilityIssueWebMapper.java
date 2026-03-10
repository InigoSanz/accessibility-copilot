package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.AccessibilityIssueResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccessibilityIssueWebMapper {

    AccessibilityIssueResponse toResponse(AccessibilityIssue issue);

    List<AccessibilityIssueResponse> toResponseList(List<AccessibilityIssue> issues);
}

