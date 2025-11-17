package com.ecomm.order.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CustomerInfo {
    private String customerId;
    private String name;
    private String email;
}
