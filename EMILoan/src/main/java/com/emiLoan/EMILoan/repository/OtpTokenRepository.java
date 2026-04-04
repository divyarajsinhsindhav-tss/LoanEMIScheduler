package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.common.enums.OtpPurpose;
import com.emiLoan.EMILoan.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findTopByEmailAndPurposeOrderByExpiryTimeDesc(String email, OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpToken t WHERE t.expiryTime < :now")
    void deleteExpiredTokens(LocalDateTime now);
}