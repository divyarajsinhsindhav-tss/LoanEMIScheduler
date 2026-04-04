package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.user.request.UpdateEmployeeRequest;
import com.emiLoan.EMILoan.dto.user.response.EmployeeDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.EmployeeProfileMapper;
import com.emiLoan.EMILoan.repository.EmployeeProfileRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
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
    private final AuditService auditService;
    private final UserRepository userRepository;

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

        LoanOfficerResponse oldState = profileMapper.toResponse(profile);

        boolean isChanged = false;

        if (request.getSalary() != null && profile.getSalary().compareTo(request.getSalary()) != 0) {
            profile.setSalary(request.getSalary());
            log.info("Updated salary for employee {}", userCode);
            isChanged = true;
        }

        if (request.getIsActive() != null && !request.getIsActive().equals(profile.getIsActive())) {
            profile.setIsActive(request.getIsActive());
            profile.getUser().setIsActive(request.getIsActive());
            log.info("Updated active status for employee {} to {}", userCode, request.getIsActive());
            isChanged = true;
        }

        EmployeeProfile savedProfile = profileRepository.save(profile);

        LoanOfficerResponse newState = profileMapper.toResponse(savedProfile);

        if (isChanged) {
            User currentAdmin = getAuthenticatedActor();

            auditService.logAction(
                    currentAdmin,
                    AuditAction.UPDATE,
                    AuditEntityType.USER,
                    profile.getUser().getUserId(),
                    "Admin updated Loan Officer details",
                    oldState,
                    newState
            );
        }

        return newState;
    }
    private User getAuthenticatedActor() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }
}