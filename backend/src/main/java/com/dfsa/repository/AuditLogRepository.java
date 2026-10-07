package com.dfsa.repository;

import com.dfsa.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findTop10ByOrderByTimestampDesc();
    List<AuditLog> findByEntityIdAndEntityType(Long entityId, String entityType);
}
