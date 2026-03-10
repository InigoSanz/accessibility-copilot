package com.inigosanz.backend.infrastructure.adapter.in.web;

import com.inigosanz.backend.domain.model.ScanSummary;
import com.inigosanz.backend.domain.port.in.GetScanSummaryUseCase;
import com.inigosanz.backend.infrastructure.adapter.in.web.dto.ScanSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scans/{scanId}/summary")
public class ScanSummaryController {

    private final GetScanSummaryUseCase getScanSummaryUseCase;
    private final ScanSummaryWebMapper scanSummaryWebMapper;

    public ScanSummaryController(GetScanSummaryUseCase getScanSummaryUseCase, ScanSummaryWebMapper scanSummaryWebMapper) {
        this.getScanSummaryUseCase = getScanSummaryUseCase;
        this.scanSummaryWebMapper = scanSummaryWebMapper;
    }

    @GetMapping
    public ResponseEntity<ScanSummaryResponse> getSummary(@PathVariable Long scanId) {
        ScanSummary summary = getScanSummaryUseCase.getSummary(scanId);
        ScanSummaryResponse response = scanSummaryWebMapper.toResponse(summary);
        return ResponseEntity.ok(response);
    }
}

