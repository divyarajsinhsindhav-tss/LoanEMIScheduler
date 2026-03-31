package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;

public interface NotificationService {

    void sendApplicationSubmitted(User user, LoanApplication application);

    void sendLoanApproved(User user, Loan loan);

    void sendLoanRejected(User user, LoanApplication application);

    void sendPaymentReminder(User user, EmiSchedule emi);

    void sendOverdueAlert(User user, EmiSchedule emi);
    void sendWelcomeEmail(User user);
    void sendLoanClosed(User user, Loan loan);
    void sendLoginNotification(User user);
}