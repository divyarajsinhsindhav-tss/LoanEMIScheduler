package com.emiLoan.EMILoan.entity;


import com.emiLoan.EMILoan.common.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE notifications SET is_deleted = true, deleted_at = NOW() WHERE notification_id = ?")
@SQLRestriction("is_deleted = false")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "loan_id")
    private UUID loanId;

    @Column(name = "emi_id")
    private UUID emiId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}