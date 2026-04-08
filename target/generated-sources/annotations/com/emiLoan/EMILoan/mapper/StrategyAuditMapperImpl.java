package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T14:51:51+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class StrategyAuditMapperImpl implements StrategyAuditMapper {

    @Override
    public StrategyAuditResponse toResponse(StrategyAudit strategyAudit) {
        if ( strategyAudit == null ) {
            return null;
        }

        StrategyAuditResponse.StrategyAuditResponseBuilder strategyAuditResponse = StrategyAuditResponse.builder();

        strategyAuditResponse.applicationId( strategyAuditApplicationApplicationId( strategyAudit ) );
        strategyAuditResponse.applicationCode( strategyAuditApplicationApplicationCode( strategyAudit ) );
        strategyAuditResponse.changedByOfficerName( mapFullName( strategyAudit.getChangedBy() ) );
        strategyAuditResponse.id( strategyAudit.getId() );
        strategyAuditResponse.systemStrategy( strategyAudit.getSystemStrategy() );
        strategyAuditResponse.officerStrategy( strategyAudit.getOfficerStrategy() );
        strategyAuditResponse.overridden( strategyAudit.isOverridden() );
        strategyAuditResponse.changedAt( strategyAudit.getChangedAt() );

        return strategyAuditResponse.build();
    }

    private UUID strategyAuditApplicationApplicationId(StrategyAudit strategyAudit) {
        LoanApplication application = strategyAudit.getApplication();
        if ( application == null ) {
            return null;
        }
        return application.getApplicationId();
    }

    private String strategyAuditApplicationApplicationCode(StrategyAudit strategyAudit) {
        LoanApplication application = strategyAudit.getApplication();
        if ( application == null ) {
            return null;
        }
        return application.getApplicationCode();
    }
}
