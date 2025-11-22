package com.ecomm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PUBLIC / IDENTIFIABLE USERNAME */
    @ToString.Include
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** LOGIN EMAIL */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** BCRYPT PASSWORD HASH */
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    /** OPTIONAL PHONE NUMBER */
    private String phone;

    /** ACCOUNT STATUS FLAGS */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    /** TOKEN VERSION FOR JWT INVALIDATION */
    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 1;

    /**
     * USER PROFILE (NAME, AVATAR, ADDRESS, CITY, PUBLIC INFO, ETC)
     * One-to-One
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private UserProfile profile;

    /**
     * ROLES (ROLE_USER, ROLE_ADMIN, ROLE_MANAGER, etc.)
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    // ===============================
    // Helper Methods
    // ===============================

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public void incrementTokenVersion() {
        this.tokenVersion += 1;
    }

    // ===============================
    // equals & hashCode (Best Practice)
    // ===============================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
