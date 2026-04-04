package com.emiLoan.EMILoan.dto.strategyAudit;


import com.fasterxml.jackson.annotation.JsonInclude;
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
public class StrategyAuditResponse {

    private UUID id;

    private UUID applicationId;
    private String applicationCode;

    private String systemStrategy;
    private String officerStrategy;
    private boolean overridden;

    private String changedByOfficerName;
    private LocalDateTime changedAt;
}