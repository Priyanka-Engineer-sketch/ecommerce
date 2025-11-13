package com.ecomm.repository;

import com.ecomm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByPhone(String phone);

    // Login/activation lookup: email (case-insensitive) or phone, and must be active
    @Query("""
            select u from User u
            where (lower(u.email) = lower(:identifier) or u.phone = :identifier)
              and u.isActive = true
            """)
    Optional<User> findActiveByIdentifier(@Param("identifier") String identifier);

    // Admin search with pagination; optional 'active' filter

    @Query("""
      select u from User u
      where lower(u.email) like lower(concat('%', :q, '%'))
         or lower(u.username) like lower(concat('%', :q, '%'))
         or lower(u.phone) like lower(concat('%', :q, '%'))
      """)
    Page<User> search(@Param("q") String q, Pageable pageable);

// Eagerly fetch roles (and permissions) only when needed for auth
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> fetchWithRolesByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from User u where u.phone = :phone")
    Optional<User> fetchWithRolesByPhone(@Param("phone") String phone);

    @Query("""
        select distinct u
        from User u
        left join fetch u.roles r
        left join fetch r.permissions
        where upper(u.email) = upper(:email)
        """)
    Optional<User> findAuthUserByEmail(@Param("email") String email);
}
