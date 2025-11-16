package com.ecomm.order.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo {
    private String customerId;
    private String name;
    private String email;
}
