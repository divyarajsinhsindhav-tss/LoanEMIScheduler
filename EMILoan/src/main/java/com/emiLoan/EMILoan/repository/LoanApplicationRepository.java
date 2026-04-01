
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

    Optional<LoanApplication> findByBorrowerEmailAndApplicationCode(String email, String applicationCode);

    Page<LoanApplication> findByBorrowerEmailAndStatus(String email, ApplicationStatus status, Pageable pageable);

    @Query("SELECT l FROM LoanApplication l WHERE l.borrower.email = :email ORDER BY l.appliedAt DESC")
    Page<LoanApplication> findByBorrowerEmailPaginated(@Param("email") String email, Pageable pageable);

    Page<LoanApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    Optional<LoanApplication> findByApplicationCode(String applicationCode);

    Long countByStatus(ApplicationStatus status);

}
