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
import com.emiLoan.EMILoan.security.CustomUserDetails;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.emiLoan.EMILoan.common.enums.OtpPurpose;
import com.emiLoan.EMILoan.service.interfaces.OtpService;

import java.time.LocalDateTime;
import java.util.List;


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
    private final OtpService otpService;

    private User getAuthenticatedActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthanticationException("Email or password incorrect"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String otp = otpService.generateAndSaveOtp(user.getEmail(), OtpPurpose.LOGIN);
        notificationService.sendLoginOtp(user, otp);

        log.info("2FA Challenge initiated for user: {}", user.getEmail());

        return AuthResponse.builder()
                .message("OTP sent to your registered email. Please verify to complete login.")
                .email(user.getEmail())
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

        user.setIsActive(false);
        user.setDeleted(false);

        User savedUser = userRepository.saveAndFlush(user);
        entityManager.refresh(savedUser);

        BorrowerProfile profile = BorrowerProfile.builder()
                .user(savedUser)
                .monthlyIncome(request.getMonthlyIncome())
                .existingLoanCount(0)
                .build();
        borrowerProfileRepository.save(profile);

        String otp = otpService.generateAndSaveOtp(savedUser.getEmail(), OtpPurpose.REGISTRATION);
        notificationService.sendRegistrationOtp(savedUser,otp);

        auditService.logAction(savedUser, AuditAction.CREATE, AuditEntityType.USER, savedUser.getUserId(),
                "Borrower self-registered (Pending OTP Verification)", null, userMapper.toResponse(savedUser));

        log.info("User {} has been created but is inactive pending OTP", savedUser.getUserCode());

        RegistrationResponse response = userMapper.toRegistrationResponse(savedUser);
        response.setMessage("Registration initiated successfully! Please check your email for the OTP to verify your account.");
        response.setVerified(false);

        return response;
    }
//    public RegistrationResponse registerBorrower(BorrowerRegistrationRequest request) {
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new BusinessRuleException("Email already exists");
//        }
//
//        String panHash = panHashingUtil.hash(request.getPan());
//        if (userRepository.existsByPerson_PanHashAndRole_RoleName(panHash, RoleName.BORROWER)) {
//            throw new BusinessRuleException("Registration failed: A borrower account with this PAN card is already registered.");
//        }
//
//        PersonIdentity person = getOrCreatePersonIdentity(request.getPan());
//        Role role = roleRepository.findByRoleName(RoleName.BORROWER)
//                .orElseThrow(() -> new BusinessRuleException("Default role not found"));
//
//        User user = userMapper.toEntity(request);
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(role);
//        user.setPerson(person);
//
//        user.setIsActive(true);
//        user.setDeleted(false);
//
//        User savedUser = userRepository.saveAndFlush(user);
//        entityManager.refresh(savedUser);
//
//        BorrowerProfile profile = BorrowerProfile.builder()
//                .user(savedUser)
//                .monthlyIncome(request.getMonthlyIncome())
//                .existingLoanCount(0)
//                .build();
//        borrowerProfileRepository.save(profile);
//
//        notificationService.sendWelcomeEmail(savedUser);
//
//        auditService.logAction(savedUser, AuditAction.CREATE, AuditEntityType.USER, savedUser.getUserId(),
//                "Borrower self-registered and account activated immediately", null, userMapper.toResponse(savedUser));
//
//        log.info("User {} has been created and activated", savedUser.getUserCode());
//
//        RegistrationResponse response = userMapper.toRegistrationResponse(savedUser);
//        response.setMessage("Registration successful! You can now log in to your account.");
//        response.setVerified(true);
//
//        return response;
//    }

    @Override
    @Transactional
    public AuthResponse verifyLoginOtp(String email, String otpCode) {
        otpService.verifyOtp(email, otpCode, OtpPurpose.LOGIN);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthanticationException("User not found after OTP verification"));

        String roleName = user.getRole().getRoleName().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        CustomUserDetails userDetails = CustomUserDetails.build(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(authority)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        notificationService.sendLoginNotification(user);

        auditService.logAction(user, AuditAction.LOGIN, AuditEntityType.USER,
                user.getUserId(), "User successfully completed 2FA login", null, null);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toShort(user))
                .build();
    }

    @Override
    @Transactional
    public RegistrationResponse verifyRegistrationOtp(String email, String otpCode) {
        otpService.verifyOtp(email, otpCode, OtpPurpose.REGISTRATION);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        if (user.getIsActive()) {
            throw new BusinessRuleException("Account is already active. Please proceed to login.");
        }

        UserResponse oldState = userMapper.toResponse(user);

        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        UserResponse newState = userMapper.toResponse(savedUser);

        notificationService.sendWelcomeEmail(savedUser);

        auditService.logAction(
                savedUser,
                AuditAction.UPDATE,
                AuditEntityType.USER,
                savedUser.getUserId(),
                "Email verified via OTP. Account activated.",
                oldState,
                newState
        );

        log.info("User {} account successfully activated via OTP", savedUser.getEmail());

        RegistrationResponse response = userMapper.toRegistrationResponse(savedUser);
        response.setMessage("Email verified successfully! Your account is now active.");
        response.setVerified(true);

        return response;
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

        User currentAdmin = getAuthenticatedActor();
        UserResponse newState = userMapper.toResponse(savedUser);
        auditService.logAction(currentAdmin, AuditAction.CREATE, AuditEntityType.USER, savedUser.getUserId(),
                "Loan Officer registered by Admin", null, newState);

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

        if (user.isDeleted()) {
            throw new BusinessRuleException("Account is already deleted.");
        }

        log.warn("Soft deleting user account: {}", email);

        UserResponse oldState = userMapper.toResponse(user);

        user.setIsActive(false);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        User currentAdmin = getAuthenticatedActor();
        if (currentAdmin != null) {
            user.setDeletedBy(currentAdmin.getUserId());
        }

        userRepository.save(user);

        if (user.getRole().getRoleName() == RoleName.LOAN_OFFICER || user.getRole().getRoleName() == RoleName.ADMIN) {
            employeeProfileRepository.findByUser_Email(email).ifPresent(profile -> {
                profile.setIsActive(false);
                employeeProfileRepository.save(profile);
            });
        }

        UserResponse newState = userMapper.toResponse(user);

        auditService.logAction(currentAdmin, AuditAction.DELETE, AuditEntityType.USER, user.getUserId(),
                "User account soft-deleted (Deactivated)", oldState, newState);

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

        if (user.getIsActive() && !user.isDeleted()) {
            throw new BusinessRuleException("This account is already active. Please use the standard login page.");
        }

        UserResponse oldState = userMapper.toResponse(user);

        user.setIsActive(true);
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        userRepository.save(user);

        if (user.getRole().getRoleName() == RoleName.LOAN_OFFICER || user.getRole().getRoleName() == RoleName.ADMIN) {
            employeeProfileRepository.findByUser_Email(request.getEmail()).ifPresent(profile -> {
                profile.setIsActive(true);
                employeeProfileRepository.save(profile);
            });
        }

        UserResponse newState = userMapper.toResponse(user);

        auditService.logAction(user, AuditAction.UPDATE, AuditEntityType.USER, user.getUserId(),
                "User recovered their deleted account", oldState, newState);

        log.info("User {} account successfully recovered and reactivated", user.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toShort(user))
                .build();
    }
}