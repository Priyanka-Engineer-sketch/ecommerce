package com.ecomm.cart.notification.kafka;

import com.ecomm.events.order.OrderCommunicationEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOutboxConsumer {

    private final JavaMailSender mailSender;

    @KafkaListener(
            topics = "user.email.outbox.v1",
            groupId = "notification-service"
    )
    public void handleOrderCommunication(OrderCommunicationEvent event) {
        log.info("Received OrderCommunicationEvent for email: {}", event);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());

            StringBuilder body = new StringBuilder();
            body.append("Hi ").append(event.getToEmail()).append(",\n\n");
            body.append("Your order #").append(event.getOrderId()).append(" is confirmed.\n\n");

            body.append("Order Summary:\n");
            event.getOrderSummary().getItemNames()
                    .forEach(n -> body.append(" - ").append(n).append("\n"));

            body.append("\nTotal Amount: ₹")
                    .append(event.getOrderSummary().getTotalAmount()).append("\n");

            if (event.getRecommendations() != null && !event.getRecommendations().isEmpty()) {
                body.append("\nRecommended for you:\n");
                event.getRecommendations().forEach(r ->
                        body.append(" - ")
                                .append(r.getName())
                                .append(" (₹").append(r.getPrice()).append(")\n")
                );
            }

            body.append("\nThank you for shopping with us!\n");

            helper.setText(body.toString());

            mailSender.send(message);
            log.info("Order confirmation email sent for order {}", event.getOrderId());

        } catch (Exception ex) {
            log.error("Failed to send order confirmation email for order {}: {}",
                    event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
