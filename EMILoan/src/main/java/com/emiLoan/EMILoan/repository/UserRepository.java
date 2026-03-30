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
    Optional<User> findByPhone(String phone);
    Optional<User> findByUserCode(String userCode);
    Optional<User> findByPerson_PersonIdAndRole_RoleId(UUID personId, UUID roleId);
    boolean existsByPerson_PersonIdAndRole_RoleId(UUID personId, UUID roleId);
    boolean existsByPerson_PanHash(String panHash);
    boolean existsByPerson_PanHashAndRole_RoleName(String panHash, RoleName roleName);
}