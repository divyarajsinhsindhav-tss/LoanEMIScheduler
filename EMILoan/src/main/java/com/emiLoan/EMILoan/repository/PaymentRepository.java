package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Payment;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByLoanOrderByPaymentDateDesc(Loan loan, Pageable pageable);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan = :loan AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByLoan(@Param("loan") Loan loan);
}