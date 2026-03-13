package com.inigosanz.backend.infrastructure.adapter.out.persistence;

import com.inigosanz.backend.domain.model.ScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaScanRepository extends JpaRepository<ScanEntity, Long> {

    List<ScanEntity> findByProjectIdOrderByStartedAtDescIdDesc(Long projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ScanEntity scan
            SET scan.status = :targetStatus,
                scan.finishedAt = :finishedAt,
                scan.errorMessage = :errorMessage
            WHERE scan.id = :scanId
              AND scan.status = :expectedStatus
            """)
    int updateStatusIfCurrentStatus(
            @Param("scanId") Long scanId,
            @Param("expectedStatus") ScanStatus expectedStatus,
            @Param("targetStatus") ScanStatus targetStatus,
            @Param("finishedAt") LocalDateTime finishedAt,
            @Param("errorMessage") String errorMessage
    );
}
