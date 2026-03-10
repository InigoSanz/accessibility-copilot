package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.ScanSummary;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScanSummaryWebMapper {

    ScanSummaryResponse toResponse(ScanSummary summary);
}

