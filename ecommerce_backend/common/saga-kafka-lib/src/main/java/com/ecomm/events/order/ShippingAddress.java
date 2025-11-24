package com.ecomm.events.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
