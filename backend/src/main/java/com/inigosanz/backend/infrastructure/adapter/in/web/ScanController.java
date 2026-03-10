package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.domain.port.in.GetScanByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListScansByProjectUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final CreateScanUseCase createScanUseCase;
    private final ListScansByProjectUseCase listScansByProjectUseCase;
    private final GetScanByIdUseCase getScanByIdUseCase;
    private final ScanWebMapper scanWebMapper;

    public ScanController(
            CreateScanUseCase createScanUseCase,
            ListScansByProjectUseCase listScansByProjectUseCase,
            GetScanByIdUseCase getScanByIdUseCase,
            ScanWebMapper scanWebMapper
    ) {
        this.createScanUseCase = createScanUseCase;
        this.listScansByProjectUseCase = listScansByProjectUseCase;
        this.getScanByIdUseCase = getScanByIdUseCase;
        this.scanWebMapper = scanWebMapper;
    }

    @PostMapping("/projects/{projectId}/scans")
    public ResponseEntity<ScanResponse> create(@PathVariable Long projectId) {
        Scan createdScan = createScanUseCase.create(projectId);
        ScanResponse response = scanWebMapper.toResponse(createdScan);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/scans")
    public ResponseEntity<List<ScanResponse>> findByProjectId(@PathVariable Long projectId) {
        List<Scan> scans = listScansByProjectUseCase.findByProjectId(projectId);
        List<ScanResponse> response = scanWebMapper.toResponseList(scans);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scans/{scanId}")
    public ResponseEntity<ScanResponse> findById(@PathVariable Long scanId) {
        Scan scan = getScanByIdUseCase.findById(scanId);
        ScanResponse response = scanWebMapper.toResponse(scan);
        return ResponseEntity.ok(response);
    }
}
