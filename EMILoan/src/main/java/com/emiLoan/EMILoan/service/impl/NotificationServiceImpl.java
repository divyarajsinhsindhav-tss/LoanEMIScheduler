package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.NotificationStatus;
import com.emiLoan.EMILoan.entity.*;
import com.emiLoan.EMILoan.repository.NotificationRepository;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    @Transactional
    @Async
    public void sendApplicationSubmitted(User user, LoanApplication application) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("amount", application.getRequestedAmount());
        props.put("appCode", application.getApplicationCode());

        sendEmail(user, null, null, "Application Received - " + application.getApplicationCode(),
                "application-submitted", props);
    }

    @Override
    @Transactional
    @Async
    public void sendApplicationWithdrawn(User user, LoanApplication application) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("appCode", application.getApplicationCode());

        sendEmail(user, null, null, "Application Withdrawn - " + application.getApplicationCode(),
                "application-withdrawn", props);
    }

    @Override
    @Transactional
    @Async
    public void sendLoanApproved(User user, Loan loan) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("loanCode", loan.getLoanCode());
        props.put("emi", loan.getEmiAmount());
        props.put("date", loan.getStartDate());

        sendEmail(user, loan, null, "Loan Approved! - " + loan.getLoanCode(),
                "loan-approved", props);
    }

    @Override
    @Transactional
    @Async
    public void sendLoanRejected(User user, LoanApplication application) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("appCode", application.getApplicationCode());

        sendEmail(user, null, null, "Update on your Loan Application",
                "loan-rejected", props);
    }

    @Override
    @Transactional
    @Async
    public void sendPaymentReminder(User user, EmiSchedule emi) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("emiAmount", emi.getTotalEmi());
        props.put("dueDate", emi.getDueDate());
        props.put("installmentNo", emi.getInstallmentNo());

        sendEmail(user, emi.getLoan(), emi, "Upcoming EMI Reminder",
                "payment-reminder", props);
    }

    @Override
    @Transactional
    @Async
    public void sendOverdueAlert(User user, EmiSchedule emi) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("loanCode", emi.getLoan().getLoanCode());
        props.put("dueDate", emi.getDueDate());

        sendEmail(user, emi.getLoan(), emi, "URGENT: EMI Overdue",
                "overdue-alert", props);
    }

    @Override
    @Transactional
    @Async
    public void sendWelcomeEmail(User user) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("role", user.getRole().getRoleName().toString());

        sendEmail(user, null, null, "Welcome to EMI Loan System!",
                "welcome-email", props);
    }

    @Override
    @Transactional
    @Async
    public void sendLoanClosed(User user, Loan loan) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("loanCode", loan.getLoanCode());
        props.put("amount", loan.getPrincipalAmount());
        props.put("closedDate", LocalDate.now());

        sendEmail(user, loan, null, "Congratulations! Your Loan is Fully Repaid - " + loan.getLoanCode(),
                "loan-closed", props);
    }

    @Override
    @Transactional
    @Async
    public void sendLoginNotification(User user) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        props.put("loginTime", LocalDateTime.now().format(formatter));

        sendEmail(user, null, null, "Security Alert: New Login Detected",
                "login-notification", props);
    }

    private void sendEmail(User user, Loan loan, EmiSchedule emi, String subject, String templateName,
                           Map<String, Object> properties) {

        Context context = new Context();
        context.setVariables(properties);

        String htmlBody = templateEngine.process(templateName, context);

        Notification notification = Notification.builder()
                .user(user)
                .loan(loan)
                .emiSchedule(emi)
                .email(user.getEmail())
                .subject(subject)
                .message(htmlBody)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING)
                .build();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            log.info("Email successfully sent to {}", user.getEmail());

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        } finally {
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    @Async
    public void sendPaymentConfirmation(User user, Payment payment) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("loanCode", payment.getLoan().getLoanCode());
        props.put("amountPaid", payment.getAmountPaid());
        props.put("paymentMode", payment.getPaymentMode().toString());
        props.put("installmentNo", payment.getEmiSchedule().getInstallmentNo());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        props.put("paymentDate", LocalDateTime.now().format(formatter));

        sendEmail(user, payment.getLoan(), payment.getEmiSchedule(),
                "Payment Receipt: ₹" + payment.getAmountPaid() + " Received",
                "payment-confirmation", props);
    }

    @Override
    @Transactional
    @Async
    public void sendPaymentFailed(User user, Payment payment) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("loanCode", payment.getLoan().getLoanCode());
        props.put("amountAttempted", payment.getAmountPaid());
        props.put("paymentMode", payment.getPaymentMode().toString());

        sendEmail(user, payment.getLoan(), payment.getEmiSchedule(),
                "Payment Failed - Action Required for Loan " + payment.getLoan().getLoanCode(),
                "payment-failed", props);
    }
}