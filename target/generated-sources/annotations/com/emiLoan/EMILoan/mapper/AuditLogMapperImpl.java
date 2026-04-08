package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T11:07:39+0530",
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

        auditLogResponse.actor( mapUserDetails( auditLog.getActor() ) );
        auditLogResponse.description( auditLog.getDescription() );
        auditLogResponse.oldValue( auditLog.getOldValue() );
        auditLogResponse.newValue( auditLog.getNewValue() );
        auditLogResponse.auditId( auditLog.getAuditId() );
        auditLogResponse.action( auditLog.getAction() );
        auditLogResponse.entityType( auditLog.getEntityType() );
        auditLogResponse.entityId( auditLog.getEntityId() );
        auditLogResponse.actionTime( auditLog.getActionTime() );

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
}
