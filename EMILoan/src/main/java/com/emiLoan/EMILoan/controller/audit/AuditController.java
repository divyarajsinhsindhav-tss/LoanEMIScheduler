package com.emiLoan.EMILoan.controller.audit;


import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")

public class AuditController {

    private final AuditService auditService;


    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getEntityAuditHistory(
            @PathVariable AuditEntityType entityType,
            @PathVariable UUID entityId,
            HttpServletRequest request
    ) {
        List<AuditLog> auditLogs = auditService.getEntityAuditHistory(entityType, entityId);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit history retrieved successfully for " + entityType + " ID: " + entityId,
                request.getRequestURI(),
                auditLogs
        ));
    }


    @GetMapping("/strategy-overrides")
    public ResponseEntity<ApiResponse<List<StrategyAudit>>> getStrategyOverrides(
            HttpServletRequest request
    ) {
        List<StrategyAudit> overrides = auditService.getRecentStrategyOverrides();

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Recent strategy overrides retrieved successfully.",
                request.getRequestURI(),
                overrides
        ));
    }
}
