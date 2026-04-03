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

    @Mapping(source = "officer", target = "officer", qualifiedByName = "mapOfficerDetails")
    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);

    @Named("mapOfficerDetails")
    default AuditLogResponse.OfficerDetails mapOfficerDetails(User officer) {
        if (officer == null) {
            return AuditLogResponse.OfficerDetails.builder()
                    .name("SYSTEM")
                    .build();
        }

        String first = officer.getFirstName() != null ? officer.getFirstName() : "";
        String last = officer.getLastName() != null ? officer.getLastName() : "";
        String fullName = (first + " " + last).trim();

        return AuditLogResponse.OfficerDetails.builder()
                .officerId(officer.getUserId())
                .name(fullName.isEmpty() ? "SYSTEM" : fullName)
                .email(officer.getEmail())
                .build();
    }
}