package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    // Explicit JPQL guarantees no runtime parsing errors for nested properties
    @Query("SELECT COUNT(l) FROM LoanApplication l WHERE l.borrower.userId = :userId AND l.status = :status")
    Long countActiveApplications(@Param("userId") UUID userId, @Param("status") ApplicationStatus status);

    Optional<LoanApplication> findByBorrowerEmailAndApplicationCode(String email, String applicationCode);

    Page<LoanApplication> findByBorrowerEmailAndStatus(String email, ApplicationStatus status, Pageable pageable);

    // Converted to Pageable and explicitly mapped via JPQL
    @Query("SELECT l FROM LoanApplication l WHERE l.borrower.email = :email ORDER BY l.appliedAt DESC")
    Page<LoanApplication> findByBorrowerEmailPaginated(@Param("email") String email, Pageable pageable);

    // Standard derived queries work fine for direct properties
    Page<LoanApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    Optional<LoanApplication> findByApplicationCode(String applicationCode);

    List<LoanApplication> findByReviewedBy(User officer);

    boolean existsByBorrowerAndStatus(User borrower, ApplicationStatus status);
}