package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.repository.AuditLogRepository;
import com.emiLoan.EMILoan.repository.StrategyAuditRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService; // Added
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final StrategyAuditRepository strategyAuditRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOfficerAction(User officer, AuditAction action, AuditEntityType entityType, UUID entityId) {
        AuditLog auditLog = AuditLog.builder()
                .officer(officer)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .actionTime(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit Logged: Officer {} performed {} on {} ID: {}",
                officer.getEmail(), action, entityType, entityId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStrategyDecision(LoanApplication application, String systemSuggested,
                                    String officerChose, boolean overridden, User officer) {

        StrategyAudit strategyAudit = StrategyAudit.builder()
                .application(application)
                .systemStrategy(systemSuggested)
                .officerStrategy(officerChose)
                .overridden(overridden)
                .changedBy(officer)
                .changedAt(LocalDateTime.now())
                .build();

        strategyAuditRepository.save(strategyAudit);

        if (overridden) {
            log.warn("Strategy Override Detected: Application {} changed from {} to {} by {}",
                    application.getApplicationCode(), systemSuggested, officerChose, officer.getEmail());
        }
    }
}