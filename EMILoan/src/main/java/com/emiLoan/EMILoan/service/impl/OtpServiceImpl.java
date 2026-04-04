package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.OtpPurpose;
import com.emiLoan.EMILoan.entity.OtpToken;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.repository.OtpTokenRepository;
import com.emiLoan.EMILoan.service.interfaces.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static com.emiLoan.EMILoan.common.constants.AppConstants.OTP_EXPIRY_MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;

    @Override
    @Transactional
    public String generateAndSaveOtp(String email, OtpPurpose purpose) {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(otp);

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpTokenRepository.save(otpToken);
//        log.info("Generated {} OTP for {}", purpose, email);

        return otpCode;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode, OtpPurpose purpose) {
        OtpToken token = otpTokenRepository.findTopByEmailAndPurposeOrderByExpiryTimeDesc(email, purpose)
                .orElseThrow(() -> new BusinessRuleException("No OTP found for this email."));

        if (token.isExpired()) {
            throw new BusinessRuleException("OTP has expired. Please request a new one.");
        }

        if (!token.getOtpCode().equals(otpCode)) {
            throw new BusinessRuleException("Invalid OTP code.");
        }

        otpTokenRepository.delete(token);
        return true;
    }
}