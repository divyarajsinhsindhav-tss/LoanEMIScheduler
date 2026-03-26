package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByBorrower(User borrower);
    List<Loan> findByBorrowerAndLoanStatus(User borrower, LoanStatus status);
    Optional<Loan> findByLoanCode(String loanCode);
    Optional<Loan> findByApplicationApplicationId(UUID applicationId);
    List<Loan> findByLoanStatus(LoanStatus status);
}