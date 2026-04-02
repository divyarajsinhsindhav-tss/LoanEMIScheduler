package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface EmiService {

    EmiScheduleResponse getNextUpcomingEmi(String loanCode, String requesterEmail);

    Page<EmiScheduleResponse> getSchedule(String loanCode, String requesterEmail,Pageable pageable);

    BigDecimal getForeclosureQuote(String loanCode, String requesterEmail, Pageable pageable) ;

    void generateAndSaveSchedule(Loan loan,Pageable pageable);
}