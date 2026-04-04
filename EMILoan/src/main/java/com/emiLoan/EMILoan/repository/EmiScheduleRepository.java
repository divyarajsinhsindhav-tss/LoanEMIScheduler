package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, UUID> {
    List<EmiSchedule> findByDueDateBeforeAndStatus(LocalDate date, EmiStatus status);

    Optional<EmiSchedule> findFirstByLoanAndStatusNotOrderByInstallmentNoAsc(Loan loan, EmiStatus status);

    Optional<EmiSchedule> findFirstByLoan_LoanCodeAndStatusOrderByDueDateAsc(String loanCode, EmiStatus status);

    Page<EmiSchedule> findByLoanOrderByInstallmentNoAsc(Loan loan, Pageable pageable);

    boolean existsByLoanAndStatusNot(Loan loan, EmiStatus status);

    List<EmiSchedule> findByStatusAndDueDateBetween(EmiStatus status, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(e) FROM EmiSchedule e WHERE e.loan.borrower.email = :email " +
            "AND e.status != 'PAID' AND e.dueDate <= :targetDate")
    Integer countUpcomingPayments(@Param("email") String email, @Param("targetDate") LocalDate targetDate);

    Optional<EmiSchedule> findFirstByLoanAndStatusInOrderByInstallmentNoAsc(Loan loan, List<EmiStatus> statuses);

    Page<EmiSchedule> findAllByLoanAndStatusInOrderByInstallmentNoAsc(Loan loan, List<EmiStatus> statuses,Pageable pageable);
}