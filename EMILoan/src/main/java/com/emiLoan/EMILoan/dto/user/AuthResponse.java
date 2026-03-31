package com.emiLoan.EMILoan.dto.user;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import static com.emiLoan.EMILoan.common.constants.AppConstants.BEARER_PREFIX;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType = BEARER_PREFIX;
    private UserResponse user;
}