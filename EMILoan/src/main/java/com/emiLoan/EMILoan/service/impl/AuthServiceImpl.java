package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.dto.user.AuthResponse;
import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoginRequest;
import com.emiLoan.EMILoan.dto.user.response.RegistrationResponse;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.entity.*;
import com.emiLoan.EMILoan.exceptions.AuthanticationException;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.UserMapper;
import com.emiLoan.EMILoan.repository.*;
import com.emiLoan.EMILoan.security.JwtTokenProvider;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import com.emiLoan.EMILoan.utils.PANHashingUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    private final NotificationService notificationService;
    private final AuditService auditService;

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

        notificationService.sendLoginNotification(user);

        if (user.getRole().getRoleName() == RoleName.LOAN_OFFICER || user.getRole().getRoleName() == RoleName.ADMIN) {
            log.info("Staff Login detected: {}", user.getEmail());
        }

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public RegistrationResponse registerBorrower(BorrowerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists");
        }

        String panHash = panHashingUtil.hash(request.getPan());
        if (userRepository.existsByPerson_PanHashAndRole_RoleName(panHash, RoleName.BORROWER)) {
            throw new BusinessRuleException("Registration failed: A borrower account with this PAN card is already registered.");
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
        notificationService.sendWelcomeEmail(savedUser);
        auditService.logSystemAction(AuditAction.CREATE, AuditEntityType.USER, savedUser.getUserId());
        log.info("User {} has been created", savedUser.getUserCode());
        return userMapper.toRegistrationResponse(savedUser);
    }

    @Override
    @Transactional
    public RegistrationResponse registerLoanOfficer(LoanOfficerRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already exists");
        }

        String panHash = panHashingUtil.hash(request.getPan());
        if (userRepository.existsByPerson_PanHashAndRole_RoleName(panHash, RoleName.LOAN_OFFICER)) {
            throw new BusinessRuleException("Registration failed: A loan officer account with this PAN card is already registered.");
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

        notificationService.sendWelcomeEmail(savedUser);
        auditService.logSystemAction(AuditAction.CREATE, AuditEntityType.USER, savedUser.getUserId());

        return userMapper.toRegistrationResponse(savedUser);
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


    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new BusinessRuleException("Account is already deleted.");
        }

        log.warn("Soft deleting user account: {}", email);

        user.setIsActive(false);
        userRepository.save(user);

        if (user.getRole().getRoleName() == RoleName.LOAN_OFFICER || user.getRole().getRoleName() == RoleName.ADMIN) {
            employeeProfileRepository.findByUser_Email(email).ifPresent(profile -> {
                profile.setIsActive(false);
                employeeProfileRepository.save(profile);
            });
        }

        auditService.logSystemAction(AuditAction.DELETE, AuditEntityType.USER, user.getUserId());
        log.info("User {} successfully soft-deleted (deactivated)", email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse recoverAccount(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthanticationException("Email or password incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthanticationException("Email or password incorrect");
        }

        if (user.getIsActive()) {
            throw new BusinessRuleException("This account is already active. Please use the standard login page.");
        }

        user.setIsActive(true);
        userRepository.save(user);

        if (user.getRole().getRoleName() == RoleName.LOAN_OFFICER || user.getRole().getRoleName() == RoleName.ADMIN) {
            employeeProfileRepository.findByUser_Email(request.getEmail()).ifPresent(profile -> {
                profile.setIsActive(true);
                employeeProfileRepository.save(profile);
            });
        }

        auditService.logSystemAction(AuditAction.UPDATE, AuditEntityType.USER, user.getUserId());
        log.info("User {} account successfully recovered", user.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();
    }

}