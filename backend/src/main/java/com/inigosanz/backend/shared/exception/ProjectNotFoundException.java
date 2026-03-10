package com.inigosanz.backend.shared.exception;

public class ProjectNotFoundException extends DomainException {

    public ProjectNotFoundException() {
        super("Project not found");
    }
}

