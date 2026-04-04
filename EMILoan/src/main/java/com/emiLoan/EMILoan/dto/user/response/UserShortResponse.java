package com.emiLoan.EMILoan.dto.user.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortResponse {
    private String userCode;
    private String firstName;
    private String lastName;
    private String role;
}