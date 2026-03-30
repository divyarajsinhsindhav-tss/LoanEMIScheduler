package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
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
    Optional<EmiSchedule> findFirstByLoan_LoanIdAndStatusOrderByDueDateAsc(UUID loanId, EmiStatus status);
    Optional<EmiSchedule> findFirstByLoan_LoanCodeAndStatusOrderByDueDateAsc(String loanCode, EmiStatus status);
    List<EmiSchedule> findByLoanOrderByInstallmentNoAsc(Loan loan);
    @Modifying
    @Query("UPDATE EmiSchedule e SET e.status = 'OVERDUE' " +
            "WHERE e.dueDate < :currentDate AND e.status = 'PENDING'")
    int updateStatusToOverdueForPastDue(@Param("currentDate") LocalDate currentDate);
    boolean existsByLoanAndStatusNot(Loan loan, EmiStatus status);
    List<EmiSchedule> findByStatusAndDueDateBetween(EmiStatus status, LocalDate startDate, LocalDate endDate);
}