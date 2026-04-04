package com.emiLoan.EMILoan.scheduler;

import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.repository.EmployeeProfileRepository;
import com.emiLoan.EMILoan.repository.OtpTokenRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    @Scheduled(cron = "0 */5 * * * *")

    @Transactional
    public void cleanUpExpiredData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime userCutoff = now.minusMinutes(5);

        log.info("Starting maintenance: Cleaning unverified data older than {}", userCutoff);

        try {
            otpTokenRepository.deleteExpiredTokens(now);

            userRepository.hardDeleteUnverifiedAccounts(userCutoff);
//            borrowerProfileRepository.deleteProfilesForUnverifiedUsers(userCutoff);
//            employeeProfileRepository.deleteEmployeeProfilesForUnverifiedUsers(userCutoff);
//               userRepository.hardDeleteUnverifiedAccounts();

            log.info("Cleanup completed: Expired OTPs and unverified accounts successfully removed.");
        } catch (Exception e) {
            log.error("Cleanup failed during scheduled task: {}", e.getMessage());
        }
    }
}