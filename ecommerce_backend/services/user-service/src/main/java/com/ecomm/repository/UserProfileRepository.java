package com.ecomm.repository;

import com.ecomm.entity.UserProfile;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Fetch by the owning user's id
    Optional<UserProfile> findByUser_Id(Long userId);

    // Handy when you only know the email
    @Query("select up from UserProfile up join up.user u where lower(u.email) = lower(:email)")
    Optional<UserProfile> findByUserEmailIgnoreCase(@Param("email") String email);

    boolean existsByUser_Id(Long userId);
}

