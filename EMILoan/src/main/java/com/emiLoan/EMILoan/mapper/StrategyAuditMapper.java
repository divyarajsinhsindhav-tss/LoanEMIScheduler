package com.emiLoan.EMILoan.mapper;


import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StrategyAuditMapper {
    @Mapping(target = "applicationId", source = "application.applicationId")
    @Mapping(target = "applicationCode", source = "application.applicationCode")
    @Mapping(target = "changedByOfficerName", expression = "java(strategyAudit.getChangedBy().getFirstName() + \" \" + strategyAudit.getChangedBy().getLastName())")
    StrategyAuditResponse toResponse(StrategyAudit strategyAudit);

    List<StrategyAuditResponse> toResponseList(List<StrategyAudit> strategyAudits);
}
