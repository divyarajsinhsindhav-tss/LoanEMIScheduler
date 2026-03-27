package com.emiLoan.EMILoan.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "person_identity")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PersonIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "person_id", updatable = false, nullable = false)
    private UUID personId;

    @Column(name = "person_code", unique = true, insertable = false, updatable = false)
    private String personCode;

    @Column(name = "pan_hash", unique = true, nullable = false, length = 64)
    private String panHash;

    @Column(name = "pan_first3", nullable = false, length = 3)
    private String panFirst3;

    @Column(name = "pan_last2", nullable = false, length = 2)
    private String panLast2;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

