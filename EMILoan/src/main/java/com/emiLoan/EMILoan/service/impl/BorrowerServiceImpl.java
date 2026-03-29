package com.emiLoan.EMILoan.service.impl;


import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.BorrowerProfileMapper;
import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.service.interfaces.BorrowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BorrowerServiceImpl implements BorrowerService {

    private final BorrowerProfileRepository borrowerProfileRepository;
    private final BorrowerProfileMapper borrowerProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public BorrowerResponse getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessRuleException("User not authenticated");
        }

        String email = authentication.getName();

        BorrowerProfile profile = borrowerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessRuleException("Borrower profile not found for the authenticated user."));

        return borrowerProfileMapper.toResponse(profile);
    }
}