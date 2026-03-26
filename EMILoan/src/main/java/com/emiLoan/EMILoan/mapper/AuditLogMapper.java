package com.emiLoan.EMILoan.mapper;


import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    @Mapping(target = "officerId", source = "officer.userId")
    @Mapping(target = "officerEmail", source = "officer.email")
    @Mapping(target = "officerName", expression = "java(auditLog.getOfficer().getFirstName() + \" \" + auditLog.getOfficer().getLastName())")
    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);
}