package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowerProfileRepository extends JpaRepository<BorrowerProfile, UUID> {
    Optional<BorrowerProfile> findByUser_UserId(UUID userId);

    @Query("SELECT b FROM BorrowerProfile b JOIN FETCH b.user WHERE b.user.email = :email")
    Optional<BorrowerProfile> findByUser_EmailWithUser(@Param("email") String email);

    Optional<BorrowerProfile> findByUser_UserCode(String userCode);
}
