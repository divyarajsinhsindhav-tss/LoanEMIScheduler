package com.emiLoan.EMILoan.repository;


import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface StrategyAuditRepository extends JpaRepository<StrategyAudit, UUID> {
    Optional<StrategyAudit> findByApplication(LoanApplication application);
    List<StrategyAudit> findByOverriddenTrue();
    List<StrategyAudit> findByChangedByOrderByChangedAtDesc(User officer);
    Optional<StrategyAudit> findByApplicationApplicationId(UUID applicationId);
}
