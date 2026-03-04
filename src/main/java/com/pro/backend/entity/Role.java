package com.pro.backend.entity;

import com.pro.backend.constants.FieldConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Role {

    @Id
    private Short id;

    @Column(nullable = false, unique = true, length = FieldConstants.ROLE_NAME_MAX)
    private String name;

    public Role(Short id, String name) {
        this.id = id;
        this.name = name;
    }
}
