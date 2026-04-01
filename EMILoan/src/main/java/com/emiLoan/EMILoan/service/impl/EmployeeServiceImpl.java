package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.user.request.UpdateEmployeeRequest;
import com.emiLoan.EMILoan.dto.user.response.EmployeeDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.EmployeeProfileMapper;
import com.emiLoan.EMILoan.repository.EmployeeProfileRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.service.interfaces.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeProfileRepository profileRepository;
    private final EmployeeProfileMapper profileMapper;

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public LoanOfficerResponse getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployeeProfile profile = profileRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessRuleException("Employee Profile Not Found"));

        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getDashboardStats() {
        Long pendingApplications = loanApplicationRepository.countByStatus(ApplicationStatus.PENDING);

        Long activeLoans = loanRepository.countByLoanStatus(LoanStatus.ACTIVE);

        Long overdueLoans = loanRepository.countByHasOverdueEmisTrue();

        return EmployeeDashboardResponse.builder()
                .pendingApplicationsCount(pendingApplications != null ? pendingApplications : 0L)
                .activeLoansCount(activeLoans != null ? activeLoans : 0L)
                .overdueLoansCount(overdueLoans != null ? overdueLoans : 0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanOfficerResponse> getAllEmployees(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeProfile> profiles = profileRepository.findAll(pageable);
        return profiles.map(profileMapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public LoanOfficerResponse getEmployeeByUserCode(String userCode) {
        EmployeeProfile profile = profileRepository.findByUser_UserCode(userCode)
                .orElseThrow(() -> new BusinessRuleException("Employee not found with code: " + userCode));
        return profileMapper.toResponse(profile);
    }


    @Override
    @Transactional
    public LoanOfficerResponse updateEmployeeDetails(String userCode, UpdateEmployeeRequest request) {
        EmployeeProfile profile = profileRepository.findByUser_UserCode(userCode)
                .orElseThrow(() -> new BusinessRuleException("Employee not found with code: " + userCode));

        if (request.getSalary() != null) {
            profile.setSalary(request.getSalary());
            log.info("Updated salary for employee {}", userCode);
        }

        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
            profile.getUser().setIsActive(request.getIsActive());
            log.info("Updated active status for employee {} to {}", userCode, request.getIsActive());
        }

        EmployeeProfile savedProfile = profileRepository.save(profile);
        return profileMapper.toResponse(savedProfile);
    }
}