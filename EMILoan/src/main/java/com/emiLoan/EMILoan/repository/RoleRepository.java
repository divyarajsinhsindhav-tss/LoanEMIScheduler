package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleName(RoleName roleName);
}