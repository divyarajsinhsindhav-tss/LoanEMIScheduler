package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @Query("SELECT l FROM Loan l WHERE l.borrower.email = :email ORDER BY l.createdAt DESC")
    List<Loan> findByBorrowerEmail(@Param("email") String email);

    @Query("SELECT l FROM Loan l WHERE l.application.applicationId = :applicationId")
    Optional<Loan> findByApplicationId(@Param("applicationId") UUID applicationId);

    @Query("SELECT l FROM Loan l WHERE l.borrower.email = :email AND l.loanStatus = :status")
    List<Loan> findByBorrowerEmailAndStatus(@Param("email") String email, @Param("status") LoanStatus status);

    Optional<Loan> findByLoanCode(String loanCode);

    List<Loan> findByLoanStatus(LoanStatus status);
}