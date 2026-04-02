package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.StrategyAudit;
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StrategyAuditRepository extends JpaRepository<StrategyAudit, UUID> {
    Page<StrategyAudit> findByOverriddenTrueOrderByChangedAtDesc(Pageable pageable);
}