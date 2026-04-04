package com.emiLoan.EMILoan.dto.auditLogs;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {

    private UUID auditId;
    private AuditAction action;
    private AuditEntityType entityType;
    private UUID entityId;
    private LocalDateTime actionTime;

    private String description;
    private Object oldValue;
    private Object newValue;

    private UserDetails actor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetails {
        private UUID userId;
        private String name;
        private String email;
        private String role;
    }
}