package com.ecomm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // SHIPPING, BILLING
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name="user_profile_id")
    private UserProfile userProfile;
}

