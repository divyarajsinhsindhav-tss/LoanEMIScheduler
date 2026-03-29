package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.EmployeeProfileMapper;
import com.emiLoan.EMILoan.repository.EmployeeProfileRepository;
import com.emiLoan.EMILoan.service.interfaces.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeProfileRepository profileRepository;
    private final EmployeeProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public LoanOfficerResponse getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeProfile profile = profileRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessRuleException("Employee Profile Not Found"));

        return profileMapper.toResponse(profile);
    }
}