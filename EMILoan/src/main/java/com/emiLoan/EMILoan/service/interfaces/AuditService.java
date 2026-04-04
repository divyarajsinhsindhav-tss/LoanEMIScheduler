package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AuditService {

    Page<AuditLogResponse> getEntityAuditHistory(AuditEntityType entityType, UUID entityId, Pageable pageable);

    void logSystemAction(AuditAction action, AuditEntityType entityType, UUID entityId);

    void logStrategyDecision(LoanApplication application, String systemSuggested, String officerChose, boolean overridden, User officer);

    Page<StrategyAuditResponse> getRecentStrategyOverrides(Pageable pageable);

    Page<AuditLogResponse> getAllAuditLogs(int page, int size);

    Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable);

    Page<AuditLogResponse> getAuditLogsByActor(UUID actorId, Pageable pageable);

    void logAction(User actor, AuditAction action, AuditEntityType entityType,
                   UUID entityId, String description, Object oldState, Object newState);
}