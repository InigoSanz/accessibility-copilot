package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.Scan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScanPersistenceMapper {

    ScanEntity toEntity(Scan scan);

    Scan toDomain(ScanEntity scanEntity);
}

