package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.entity.*;

public interface NotificationService {

    void sendApplicationSubmitted(User user, LoanApplication application);

    void sendLoanApproved(User user, Loan loan);

    void sendLoanRejected(User user, LoanApplication application);

    void sendPaymentReminder(User user, EmiSchedule emi);

    void sendOverdueAlert(User user, EmiSchedule emi);

    void sendWelcomeEmail(User user);

    void sendLoanClosed(User user, Loan loan);

    void sendLoginNotification(User user);

    void sendPaymentConfirmation(User user, Payment payment);

    void sendPaymentFailed(User user, Payment payment);

    void sendApplicationWithdrawn(User user, LoanApplication application);

    void sendRegistrationOtp(User user, String otp);

    void sendLoginOtp(User user, String otp);
}