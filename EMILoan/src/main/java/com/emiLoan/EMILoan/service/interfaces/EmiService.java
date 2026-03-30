package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.Loan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmiService {

    List<EmiScheduleResponse> getSchedule(String loanCode, String email);

    void generateAndSaveSchedule(Loan loan);

    void processOverdueEmis(LocalDate currentDate);
}