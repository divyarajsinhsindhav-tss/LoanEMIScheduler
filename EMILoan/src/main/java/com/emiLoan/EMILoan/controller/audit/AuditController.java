package com.emiLoan.EMILoan.controller.audit;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getEntityAuditHistory(
            @PathVariable AuditEntityType entityType,
            @PathVariable UUID entityId,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "3") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<AuditLogResponse> auditLogs = auditService.getEntityAuditHistory(entityType, entityId, pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit history retrieved successfully for " + entityType + " ID: " + entityId,
                request.getRequestURI(),
                auditLogs
        ));
    }

    @GetMapping("/strategy-overrides")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<Page<StrategyAuditResponse>>> getStrategyOverrides(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<StrategyAuditResponse> overrides = auditService.getRecentStrategyOverrides(pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Recent strategy overrides retrieved successfully.",
                request.getRequestURI(),
                overrides
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Page<AuditLogResponse> auditLogs = auditService.getAllAuditLogs(page, size);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Master audit log retrieved successfully",
                request.getRequestURI(),
                auditLogs
        ));
    }

    @GetMapping("/actor/{actorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByActor(
            @PathVariable UUID actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> auditLogs = auditService.getAuditLogsByActor(actorId, pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit logs retrieved successfully for user ID: " + actorId,
                request.getRequestURI(),
                auditLogs
        ));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogsByAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> auditLogs = auditService.getAuditLogsByAction(action, pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit logs retrieved successfully for action: " + action,
                request.getRequestURI(),
                auditLogs
        ));
    }
}