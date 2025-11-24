package com.ecomm.order.dto;

import com.ecomm.events.payment.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull CustomerDto customer,               // will be overridden by userId from JWT
        @NotNull ShippingAddressDto shippingAddress,
        @NotEmpty List<OrderItemDto> items,
        PaymentMethod paymentMethod
) {

    public record CustomerDto(
            @NotNull String id,
            @NotNull String name,
            @NotNull String email
    ) {}

    public record ShippingAddressDto(
            @NotNull String line1,
            String line2,
            @NotNull String city,
            @NotNull String state,
            @NotNull String postalCode,
            @NotNull String country
    ) {}

    public record OrderItemDto(
            @NotNull String id,
            @NotNull String name,
            double price,
            int quantity
    ) {}
}
