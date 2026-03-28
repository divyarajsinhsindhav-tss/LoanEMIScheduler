package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StrategyAuditMapper {

    @Mapping(target = "applicationId", source = "application.applicationId")
    @Mapping(target = "applicationCode", source = "application.applicationCode")
    @Mapping(target = "changedByOfficerName", source = "changedBy", qualifiedByName = "mapFullName")
    StrategyAuditResponse toResponse(StrategyAudit strategyAudit);

    List<StrategyAuditResponse> toResponseList(List<StrategyAudit> strategyAudits);


    @Named("mapFullName")
    default String mapFullName(User user) {
        if (user == null) return "SYSTEM";
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }
}