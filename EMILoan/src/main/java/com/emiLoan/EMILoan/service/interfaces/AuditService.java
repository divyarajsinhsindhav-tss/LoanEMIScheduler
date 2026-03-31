package com.emiLoan.EMILoan.service.interfaces;



import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;

import java.util.List;
import java.util.UUID;

public interface AuditService {

    void logOfficerAction(User officer, AuditAction action, AuditEntityType entityType, UUID entityId);

    void logSystemAction(AuditAction action, AuditEntityType entityType, UUID entityId);

    void logStrategyDecision(LoanApplication application, String systemSuggested, String officerChose, boolean overridden, User officer);

    List<AuditLog> getEntityAuditHistory(AuditEntityType entityType, UUID entityId);

    List<StrategyAudit> getRecentStrategyOverrides();
}