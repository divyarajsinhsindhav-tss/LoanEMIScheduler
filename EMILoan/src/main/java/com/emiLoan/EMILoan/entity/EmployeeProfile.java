package com.emiLoan.EMILoan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_profile")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "employee_id", updatable = false, nullable = false)
    private UUID employeeId;

    @Column(name = "employee_code", unique = true, insertable = false, updatable = false)
    private String employeeCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}