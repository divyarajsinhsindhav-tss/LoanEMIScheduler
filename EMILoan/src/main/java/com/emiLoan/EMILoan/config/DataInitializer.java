package com.emiLoan.EMILoan.config;

import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.entity.PersonIdentity;
import com.emiLoan.EMILoan.entity.Role;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.repository.PersonIdentityRepository;
import com.emiLoan.EMILoan.repository.RoleRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.utils.PANHashingUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PersonIdentityRepository personIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PANHashingUtil panHashingUtil;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeAdmin();
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .roleName(roleName)
                        .description("Default " + roleName.name() + " role")
                        .build();
                roleRepository.save(role);
                log.info("Initialized role: {}", roleName);
            }
        }
    }

    private void initializeAdmin() {
        String adminEmail = "admin@loan.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found after initialization"));

            String dummyPan = "ADMIN1234P";
            String panHash = panHashingUtil.hash(dummyPan);
            
            PersonIdentity adminPerson = personIdentityRepository.findByPanHash(panHash)
                    .orElseGet(() -> {
                        PersonIdentity newPerson = PersonIdentity.builder()
                                .panHash(panHash)
                                .panFirst3(panHashingUtil.extractFirst3(dummyPan))
                                .panLast2(panHashingUtil.extractLast2(dummyPan))
                                .build();
                        PersonIdentity savedPerson = personIdentityRepository.saveAndFlush(newPerson);
                        entityManager.refresh(savedPerson);
                        return savedPerson;
                    });

            User adminUser = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Password123"))
                    .phone("0000000000")
                    .role(adminRole)
                    .person(adminPerson)
                    .isActive(true)
                    .build();

            User savedAdmin = userRepository.saveAndFlush(adminUser);
            entityManager.refresh(savedAdmin);
            
            log.info("Initialized first admin user: {} with code: {}", adminEmail, savedAdmin.getUserCode());
        }
    }
}
