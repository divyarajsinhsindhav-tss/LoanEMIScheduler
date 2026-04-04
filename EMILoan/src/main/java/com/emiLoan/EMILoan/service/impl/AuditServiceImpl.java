package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.mapper.AuditLogMapper;
import com.emiLoan.EMILoan.mapper.StrategyAuditMapper;
import com.emiLoan.EMILoan.repository.AuditLogRepository;
import com.emiLoan.EMILoan.repository.StrategyAuditRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
    private final StrategyAuditMapper strategyAuditMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void logAction(User actor, AuditAction action, AuditEntityType entityType,
                          UUID entityId, String description, Object oldState, Object newState) {

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .oldValue(oldState)
                .newValue(newState)
                .actionTime(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        String actorEmail = (actor != null) ? actor.getEmail() : "SYSTEM";
        log.info("Audit Logged: [{}] - {} performed {} on {} (ID: {})",
                actorEmail, description, action, entityType, entityId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void logSystemAction(AuditAction action, AuditEntityType entityType, UUID entityId) {
        this.logAction(null, action, entityType, entityId, "Automated system action", null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
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

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getEntityAuditHistory(AuditEntityType entityType, UUID entityId, Pageable pageable) {
        Page<AuditLog> auditLogResponsePage = auditLogRepository.findByEntityTypeAndEntityIdOrderByActionTimeDesc(entityType, entityId, pageable);
        return auditLogResponsePage.map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByActor(UUID actorId, Pageable pageable) {
        return auditLogRepository.findByActorUserIdOrderByActionTimeDesc(actorId, pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StrategyAuditResponse> getRecentStrategyOverrides(Pageable pageable) {
        Page<StrategyAudit> auditPage = strategyAuditRepository.findByOverriddenTrueOrderByChangedAtDesc(pageable);
        return auditPage.map(strategyAuditMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByActionOrderByActionTimeDesc(action, pageable);
        return logs.map(auditLogMapper::toResponse);
    }

}