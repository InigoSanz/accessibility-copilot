package com.inigosanz.backend.domain.port.in;

import com.inigosanz.backend.domain.model.Scan;

public interface GetScanByIdUseCase {

    Scan findById(Long scanId);
}

