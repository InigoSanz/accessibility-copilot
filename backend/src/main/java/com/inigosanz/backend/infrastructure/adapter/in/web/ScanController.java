package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/scans")
public class ScanController {

    private final CreateScanUseCase createScanUseCase;
    private final ScanWebMapper scanWebMapper;

    public ScanController(CreateScanUseCase createScanUseCase, ScanWebMapper scanWebMapper) {
        this.createScanUseCase = createScanUseCase;
        this.scanWebMapper = scanWebMapper;
    }

    @PostMapping
    public ResponseEntity<ScanResponse> create(@PathVariable Long projectId) {
        Scan createdScan = createScanUseCase.create(projectId);
        ScanResponse response = scanWebMapper.toResponse(createdScan);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

