package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.Loan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmiService {

    /**
     * Retrieves the full amortization schedule for a given loan.
     * Enforces data ownership checks for the borrower.
     */
    List<EmiScheduleResponse> getSchedule(String loanId);

    /**
     * Internal business method called immediately after a Loan is APPROVED and saved.
     * Generates the mathematical schedule and persists the rows to the database.
     */
    void generateAndSaveSchedule(Loan loan);

    /**
     * Background job method to mark missed payments as OVERDUE.
     */
    void processOverdueEmis(LocalDate currentDate);
}