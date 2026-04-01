package com.emiLoan.EMILoan.controller.audit;

import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getEntityAuditHistory(
            @PathVariable AuditEntityType entityType,
            @PathVariable UUID entityId,
            HttpServletRequest request
    ) {
        List<AuditLogResponse> auditLogs = auditService.getEntityAuditHistory(entityType, entityId);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit history retrieved successfully for " + entityType + " ID: " + entityId,
                request.getRequestURI(),
                auditLogs
        ));
    }

    @GetMapping("/strategy-overrides")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<List<StrategyAuditResponse>>> getStrategyOverrides(
            HttpServletRequest request
    ) {
        List<StrategyAuditResponse> overrides = auditService.getRecentStrategyOverrides();

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Recent strategy overrides retrieved successfully.",
                request.getRequestURI(),
                overrides
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ){
        Page<AuditLogResponse> auditLogs = auditService.getAllAuditLogs(page, size);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Master audit log retrieved successfully",
                request.getRequestURI(),
                auditLogs
        ));
    }
}