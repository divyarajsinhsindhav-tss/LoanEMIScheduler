package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowerProfileRepository extends JpaRepository<BorrowerProfile, UUID> {
    Optional<BorrowerProfile> findByUser(User user);
    Optional<BorrowerProfile> findByUser_UserId(UUID userId);
    Optional<BorrowerProfile> findByUser_Email(String email);
    Optional<BorrowerProfile> findByBorrowerCode(String borrowerCode);
}
