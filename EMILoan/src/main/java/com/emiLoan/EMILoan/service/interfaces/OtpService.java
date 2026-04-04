package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.common.enums.OtpPurpose;

public interface OtpService {
    String generateAndSaveOtp(String email, OtpPurpose purpose);

    boolean verifyOtp(String email, String otpCode, OtpPurpose purpose);
}
