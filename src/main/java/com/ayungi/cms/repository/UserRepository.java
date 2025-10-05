package com.ayungi.cms.repository;

import com.ayungi.cms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Поиск пользователя по имени пользователя
     *
     * @param username имя пользователя
     * @return Optional с пользователем
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск пользователя по email
     *
     * @param email email пользователя
     * @return Optional с пользователем
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверка существования пользователя по имени
     *
     * @param username имя пользователя
     * @return true если пользователь существует
     */
    boolean existsByUsername(String username);

    /**
     * Проверка существования пользователя по email
     *
     * @param email email пользователя
     * @return true если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Поиск всех активных пользователей
     *
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    Page<User> findByEnabledTrue(Pageable pageable);

    /**
     * Поиск пользователей по имени роли
     *
     * @param roleName название роли
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Поиск пользователей по части имени или email
     *
     * @param searchTerm поисковый запрос
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
