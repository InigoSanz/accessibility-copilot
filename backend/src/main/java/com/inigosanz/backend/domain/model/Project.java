package com.inigosanz.backend.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {

    private final Long id;
    private final String name;
    private final String rootUrl;
    private final LocalDateTime createdAt;

    public Project(Long id, String name, String rootUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name is required");
        this.rootUrl = Objects.requireNonNull(rootUrl, "rootUrl is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

