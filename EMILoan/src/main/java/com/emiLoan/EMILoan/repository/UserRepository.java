package com.emiLoan.EMILoan.repository;


import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    boolean existsByPerson_PanHashAndRole_RoleName(String panHash, RoleName roleName);
}