package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    List<LoanApplication> findByBorrowerOrderByAppliedAtDesc(User borrower);
    List<LoanApplication> findByStatus(ApplicationStatus status);
    Optional<LoanApplication> findByApplicationCode(String applicationCode);
    List<LoanApplication> findByReviewedBy(User officer);
    boolean existsByBorrowerAndStatus(User borrower, ApplicationStatus status);
}