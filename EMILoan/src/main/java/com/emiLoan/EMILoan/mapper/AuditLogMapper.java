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

    @Mapping(source = "actor", target = "actor", qualifiedByName = "mapUserDetails")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "oldValue", target = "oldValue")
    @Mapping(source = "newValue", target = "newValue")
    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);

    @Named("mapUserDetails")
    default AuditLogResponse.UserDetails mapUserDetails(User user) {
        if (user == null) {
            return AuditLogResponse.UserDetails.builder()
                    .name("SYSTEM")
                    .role("SYSTEM_PROCESS")
                    .build();
        }

        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (first + " " + last).trim();

        return AuditLogResponse.UserDetails.builder()
                .userId(user.getUserId())
                .name(fullName.isEmpty() ? "UNKNOWN" : fullName)
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getRoleName().name() : "USER")
                .build();
    }
}