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
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public void logOfficerAction(User officer, AuditAction action, AuditEntityType entityType, UUID entityId) {
        AuditLog auditLog = AuditLog.builder()
                .officer(officer)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .actionTime(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        String officerEmail = (officer != null) ? officer.getEmail() : "SYSTEM";
        log.info("Audit Logged: {} performed {} on {} ID: {}",
                officerEmail, action, entityType, entityId);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void logSystemAction(AuditAction action, AuditEntityType entityType, UUID entityId) {
        AuditLog auditLog = AuditLog.builder()
                .officer(null)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .actionTime(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("System Audit Logged: Automated process performed {} on {} ID: {}",
                action, entityType, entityId);
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
    public List<AuditLogResponse> getEntityAuditHistory(AuditEntityType entityType, UUID entityId) {
        return auditLogMapper.toResponseList(auditLogRepository.findByEntityTypeAndEntityIdOrderByActionTimeDesc(entityType,entityId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StrategyAuditResponse> getRecentStrategyOverrides() {
        return strategyAuditMapper.toResponseList(strategyAuditRepository.findByOverriddenTrueOrderByChangedAtDesc());
    }



    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "actionDate"));
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return logs.map(auditLogMapper::toResponse);
    }

}