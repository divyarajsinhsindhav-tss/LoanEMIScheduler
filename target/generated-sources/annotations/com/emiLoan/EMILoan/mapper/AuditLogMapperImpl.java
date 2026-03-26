package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-26T17:44:34+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Override
    public AuditLogResponse toResponse(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }

        AuditLogResponse.AuditLogResponseBuilder auditLogResponse = AuditLogResponse.builder();

        auditLogResponse.officerId( auditLogOfficerUserId( auditLog ) );
        auditLogResponse.officerEmail( auditLogOfficerEmail( auditLog ) );
        auditLogResponse.auditId( auditLog.getAuditId() );
        auditLogResponse.action( auditLog.getAction() );
        auditLogResponse.entityType( auditLog.getEntityType() );
        auditLogResponse.entityId( auditLog.getEntityId() );
        auditLogResponse.actionTime( auditLog.getActionTime() );

        auditLogResponse.officerName( auditLog.getOfficer().getFirstName() + " " + auditLog.getOfficer().getLastName() );

        return auditLogResponse.build();
    }

    @Override
    public List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs) {
        if ( auditLogs == null ) {
            return null;
        }

        List<AuditLogResponse> list = new ArrayList<AuditLogResponse>( auditLogs.size() );
        for ( AuditLog auditLog : auditLogs ) {
            list.add( toResponse( auditLog ) );
        }

        return list;
    }

    private UUID auditLogOfficerUserId(AuditLog auditLog) {
        User officer = auditLog.getOfficer();
        if ( officer == null ) {
            return null;
        }
        return officer.getUserId();
    }

    private String auditLogOfficerEmail(AuditLog auditLog) {
        User officer = auditLog.getOfficer();
        if ( officer == null ) {
            return null;
        }
        return officer.getEmail();
    }
}
