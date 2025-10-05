package com.ayungi.cms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Сущность роли пользователя
 */
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
    private Long id;

    /**
     * Название роли (ADMIN, USER)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Пользователи с данной ролью
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return name != null && name.equals(role.name);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
