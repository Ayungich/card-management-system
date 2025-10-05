package com.ayungi.cms.repository;

import com.ayungi.cms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Поиск роли по названию
     *
     * @param name название роли (ADMIN, USER)
     * @return Optional с ролью
     */
    Optional<Role> findByName(String name);

    /**
     * Проверка существования роли по названию
     *
     * @param name название роли
     * @return true если роль существует
     */
    boolean existsByName(String name);
}
