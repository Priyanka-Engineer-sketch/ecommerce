package com.ecomm.shipping.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipping")
public class ShippingController {

    @GetMapping("/health")
    public String health() {
        return "OK - shipping-service";
    }

    // TODO: later add endpoints like:
    // - GET /shipping/{orderId} to get shipping status
}
