package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    @Mapping(target = "officerId", source = "officer.userId")
    @Mapping(target = "officerEmail", source = "officer.email")
    @Mapping(target = "officerName", source = "officer", qualifiedByName = "mapFullName")
    @Mapping(target = "actionTime", source = "actionTime")
    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);

    @Named("mapFullName")
    default String mapFullName(User officer) {
        if (officer == null) return "SYSTEM";

        String first = officer.getFirstName() != null ? officer.getFirstName() : "";
        String last = officer.getLastName() != null ? officer.getLastName() : "";

        return (first + " " + last).trim();
    }
}