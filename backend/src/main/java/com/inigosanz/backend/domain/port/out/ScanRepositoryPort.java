package com.inigosanz.backend.domain.port.out;

import com.inigosanz.backend.domain.model.Scan;

public interface ScanRepositoryPort {

    Scan save(Scan scan);
}

