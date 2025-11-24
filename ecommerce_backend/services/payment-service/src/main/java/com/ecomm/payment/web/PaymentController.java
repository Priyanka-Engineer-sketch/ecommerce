package com.ecomm.payment.web;

import com.ecomm.payment.domain.Payment;
import com.ecomm.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Payment info and diagnostics")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/health")
    @Operation(summary = "Health check for payment-service")
    public String health() {
        return "OK - payment-service";
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get payment details by order id")
    public Payment getPaymentByOrder(@PathVariable Long orderId) {
        return paymentService.getByOrderId(orderId);
    }
}
