package com.emiLoan.EMILoan.entity;


import com.emiLoan.EMILoan.common.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private RoleName roleName;

    public Role(RoleName roleName) {
        this.roleName = roleName;
    }
}