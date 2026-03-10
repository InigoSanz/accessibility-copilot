package com.inigosanz.backend.shared.exception;

public class ScanNotFoundException extends DomainException {

    public ScanNotFoundException() {
        super("Scan not found");
    }
}

