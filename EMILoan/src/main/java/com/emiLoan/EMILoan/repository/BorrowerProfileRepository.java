package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowerProfileRepository extends JpaRepository<BorrowerProfile, UUID> {
    Optional<BorrowerProfile> findByUser(User user);
    Optional<BorrowerProfile> findByUser_UserId(UUID userId);
    @Query("SELECT b FROM BorrowerProfile b JOIN FETCH b.user WHERE b.user.email = :email")
    Optional<BorrowerProfile> findByUser_EmailWithUser(@Param("email") String email);

    Optional<BorrowerProfile> findByBorrowerCode(String borrowerCode);
}
