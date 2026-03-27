package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
    Optional<EmployeeProfile> findByUser(User user);
    Optional<EmployeeProfile> findByUser_UserId(UUID userId);
    Optional<EmployeeProfile> findByEmployeeCode(String employeeCode);
}
