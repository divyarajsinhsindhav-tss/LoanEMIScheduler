package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    boolean existsByPerson_PanHashAndRole_RoleName(String panHash, RoleName roleName);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE is_active = false " +
            "AND is_deleted = false " +
            "AND created_at < :cutoff", nativeQuery = true)
    void hardDeleteUnverifiedAccounts(@Param("cutoff") LocalDateTime cutoff);
}