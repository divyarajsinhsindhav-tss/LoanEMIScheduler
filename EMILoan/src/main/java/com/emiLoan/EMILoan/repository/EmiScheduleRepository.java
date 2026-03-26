package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, UUID> {
    List<EmiSchedule> findByLoanOrderByInstallmentNoAsc(Loan loan);
    Optional<EmiSchedule> findFirstByLoanAndStatusOrderByInstallmentNoAsc(Loan loan, EmiStatus status);
    List<EmiSchedule> findByDueDateAndStatus(LocalDate date, EmiStatus status);
    List<EmiSchedule> findByDueDateBeforeAndStatus(LocalDate date, EmiStatus status);
    Optional<EmiSchedule> findByLoanLoanIdAndInstallmentNo(UUID loanId, Integer installmentNo);
    boolean existsByLoanAndStatusNot(Loan loan, EmiStatus status);
}