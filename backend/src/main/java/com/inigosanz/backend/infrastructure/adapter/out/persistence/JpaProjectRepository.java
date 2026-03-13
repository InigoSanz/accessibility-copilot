package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findAllByOrderByCreatedAtDescIdDesc();
}

