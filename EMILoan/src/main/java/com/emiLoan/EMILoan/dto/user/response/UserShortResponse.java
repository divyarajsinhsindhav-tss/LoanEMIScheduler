package com.emiLoan.EMILoan.dto.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserShortResponse {
    private String userCode;
    private String firstName;
    private String lastName;
    private String role;
}