package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.ScanSummary;

public interface GetScanSummaryUseCase {

    ScanSummary getSummary(Long scanId);
}

