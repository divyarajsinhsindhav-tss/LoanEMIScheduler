package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @Query("SELECT l FROM Loan l WHERE l.borrower.email = :email ORDER BY l.createdAt DESC")
    Page<Loan> findByBorrowerEmail(@Param("email") String email,Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.application.applicationId = :applicationId")
    Optional<Loan> findByApplicationId(@Param("applicationId") UUID applicationId);

    Optional<Loan> findByLoanCode(String loanCode);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.borrower.userId = :userId AND l.loanStatus = :status")
    Long countActiveLoans(@Param("userId") UUID userId, @Param("status") LoanStatus status);

    Page<Loan> findByLoanStatus(LoanStatus status, Pageable pageable);

    Long countByBorrower_EmailAndLoanStatus(String email, LoanStatus status);

    @Query("SELECT SUM(e.totalEmi - COALESCE(e.amountPaid, 0)) FROM EmiSchedule e " +
            "WHERE e.loan.borrower.email = :email AND e.status != 'PAID'")
    BigDecimal sumRemainingTotalDebtByBorrowerEmail(@Param("email") String email);

    Long countByLoanStatus(LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE EXISTS (SELECT 1 FROM EmiSchedule e WHERE e.loan = l AND e.status = 'OVERDUE')")
    Long countByHasOverdueEmisTrue();
}