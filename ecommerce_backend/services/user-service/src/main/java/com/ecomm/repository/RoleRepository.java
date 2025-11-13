package com.ecomm.repository;

import com.ecomm.entity.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    @EntityGraph(attributePaths = "permissions")
    @Query("select r from Role r where r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
}