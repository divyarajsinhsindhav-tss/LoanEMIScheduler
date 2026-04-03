package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByActionTimeDesc(AuditEntityType entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByOfficerUserIdOrderByActionTimeDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByActionOrderByActionTimeDesc(AuditAction action, Pageable pageable);
}