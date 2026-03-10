package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScanWebMapper {

    ScanResponse toResponse(Scan scan);
}

