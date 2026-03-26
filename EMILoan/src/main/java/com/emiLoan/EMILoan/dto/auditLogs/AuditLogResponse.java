package com.emiLoan.EMILoan.dto.auditLogs;


import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID auditId;

    private UUID officerId;
    private String officerName;
    private String officerEmail;


    private AuditAction action;
    private AuditEntityType entityType;
    private UUID entityId;

    private LocalDateTime actionTime;
}