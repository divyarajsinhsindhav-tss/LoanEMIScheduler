package com.emiLoan.EMILoan.dto.user;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.dto.user.response.UserShortResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static com.emiLoan.EMILoan.common.constants.AppConstants.BEARER_PREFIX;

@Data
@Builder
@Getter
@Setter
public class AuthResponse {
    private String accessToken;
    private String tokenType = BEARER_PREFIX;
    private UserShortResponse user;
}