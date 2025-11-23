package com.ecomm.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEmailListeners {

    private final EmailService emailService;

    // ---------------- ORDER PLACED ----------------
    @Async
    @EventListener
    public void onOrderPlaced(OrderEvents.OrderPlacedEvent e) {
        try {
            String subject = "✅ Order placed: " + e.externalOrderId();
            String body = """
                    <h2>Order Confirmed</h2>
                    <p>Hi %s,</p>
                    <p>Thank you for your order. Your order has been placed successfully.</p>
                    <p><b>Order ID:</b> %s</p>
                    <p><b>Total Amount:</b> %.2f</p>
                    <p>We will notify you when your order is shipped.</p>
                    """.formatted(e.customerName(), e.externalOrderId(), e.totalAmount());

            emailService.sendHtmlEmail(e.customerEmail(), subject, body);
            log.info("Sent order-placed email for orderId={} to {}", e.orderId(), e.customerEmail());
        } catch (Exception ex) {
            log.error("Failed to send order-placed email for orderId={} to {}: {}",
                    e.orderId(), e.customerEmail(), ex.getMessage(), ex);
        }
    }

    // ---------------- ORDER CANCELLED ----------------
    @Async
    @EventListener
    public void onOrderCancelled(OrderEvents.OrderCancelledEvent e) {
        try {
            String subject = "❌ Order cancelled: " + e.externalOrderId();
            String body = """
                    <h2>Order Cancelled</h2>
                    <p>Hi %s,</p>
                    <p>Your order has been cancelled.</p>
                    <p><b>Order ID:</b> %s</p>
                    <p><b>Previous Status:</b> %s</p>
                    <p><b>Amount:</b> %.2f</p>
                    <p>If you did not request this cancellation, please contact support.</p>
                    """.formatted(
                    e.customerName(),
                    e.externalOrderId(),
                    e.previousStatus(),
                    e.totalAmount()
            );

            emailService.sendHtmlEmail(e.customerEmail(), subject, body);
            log.info("Sent order-cancelled email for orderId={} to {}", e.orderId(), e.customerEmail());
        } catch (Exception ex) {
            log.error("Failed to send order-cancelled email for orderId={} to {}: {}",
                    e.orderId(), e.customerEmail(), ex.getMessage(), ex);
        }
    }
}
