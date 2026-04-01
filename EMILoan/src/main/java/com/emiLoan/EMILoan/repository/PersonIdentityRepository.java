package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.PersonIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonIdentityRepository extends JpaRepository<PersonIdentity, UUID> {
    Optional<PersonIdentity> findByPanHash(String panHash);
}
