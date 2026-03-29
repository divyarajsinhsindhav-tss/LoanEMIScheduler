package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.dto.user.AuthResponse;
import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoginRequest;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.entity.*;
import com.emiLoan.EMILoan.exceptions.AuthanticationException;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.UserMapper;
import com.emiLoan.EMILoan.repository.*;
import com.emiLoan.EMILoan.security.JwtTokenProvider;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import com.emiLoan.EMILoan.utils.PANHashingUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PersonIdentityRepository personIdentityRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PANHashingUtil panHashingUtil;
    private final EntityManager entityManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthanticationException("Email or password incorrect"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public UserResponse registerBorrower(BorrowerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists");
        }

        PersonIdentity person = getOrCreatePersonIdentity(request.getPan());
        Role role = roleRepository.findByRoleName(RoleName.BORROWER)
                .orElseThrow(() -> new BusinessRuleException("Default role not found"));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setPerson(person);
        user.setIsActive(true);

        User savedUser = userRepository.saveAndFlush(user);
        entityManager.refresh(savedUser);

        BorrowerProfile profile = BorrowerProfile.builder()
                .user(savedUser)
                .monthlyIncome(request.getMonthlyIncome())
                .existingLoanCount(0)
                .build();
        borrowerProfileRepository.save(profile);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse registerLoanOfficer(LoanOfficerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists");
        }

        PersonIdentity person = getOrCreatePersonIdentity(request.getPan());
        Role role = roleRepository.findByRoleName(RoleName.LOAN_OFFICER)
                .orElseThrow(() -> new BusinessRuleException("Default role not found"));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setPerson(person);
        user.setIsActive(true);

        User savedUser = userRepository.saveAndFlush(user);
        entityManager.refresh(savedUser);

        EmployeeProfile profile = EmployeeProfile.builder()
                .user(savedUser)
                .joiningDate(request.getJoiningDate())
                .salary(request.getSalary())
                .isActive(true)
                .build();
        employeeProfileRepository.save(profile);

        return userMapper.toResponse(savedUser);
    }

    private PersonIdentity getOrCreatePersonIdentity(String pan) {
        String panHash = panHashingUtil.hash(pan);
        return personIdentityRepository.findByPanHash(panHash)
                .orElseGet(() -> {
                    PersonIdentity newPerson = PersonIdentity.builder()
                            .panHash(panHash)
                            .panFirst3(panHashingUtil.extractFirst3(pan))
                            .panLast2(panHashingUtil.extractLast2(pan))
                            .build();
                    PersonIdentity savedPerson = personIdentityRepository.saveAndFlush(newPerson);
                    entityManager.refresh(savedPerson);
                    return savedPerson;
                });
    }
}