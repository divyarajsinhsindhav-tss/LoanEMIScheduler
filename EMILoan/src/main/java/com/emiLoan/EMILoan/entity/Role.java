package com.emiLoan.EMILoan.entity;

import com.emiLoan.EMILoan.common.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private RoleName roleName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Role(RoleName roleName) {
        this.roleName = roleName;
    }
}