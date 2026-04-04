package com.emiLoan.EMILoan.dto.user;

import com.emiLoan.EMILoan.dto.user.response.UserShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String message;
    private String email;

    private String accessToken;
    private String tokenType;
    private UserShortResponse user;
}