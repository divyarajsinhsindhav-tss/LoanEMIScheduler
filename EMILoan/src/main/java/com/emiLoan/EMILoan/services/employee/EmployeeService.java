package com.emiLoan.EMILoan.services.employee;

import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.EmployeeProfileMapper;
import com.emiLoan.EMILoan.repository.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeProfileRepository profileRepository;
    private final EmployeeProfileMapper profileMapper;

    public LoanOfficerResponse getProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeProfile profile = profileRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessRuleException("Employee Profile Not Found"));

        return profileMapper.toResponse(profile);

    }
}
