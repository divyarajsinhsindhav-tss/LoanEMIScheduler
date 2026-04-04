package com.emiLoan.EMILoan.repository;


import com.emiLoan.EMILoan.common.enums.NotificationStatus;
import com.emiLoan.EMILoan.entity.Notification;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}