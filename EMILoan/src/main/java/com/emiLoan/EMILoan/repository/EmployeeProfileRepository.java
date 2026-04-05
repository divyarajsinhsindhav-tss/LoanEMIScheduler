package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.EmployeeProfile;
import com.emiLoan.EMILoan.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
    Optional<EmployeeProfile> findByUser_Email(String email);

    Optional<EmployeeProfile> findByUser_UserCode(String userCode);
}
