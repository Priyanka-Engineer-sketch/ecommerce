package com.ecomm.invoice.kafka;

import com.ecomm.events.order.OrderPlacedEvent;
import com.ecomm.invoice.domain.Invoice;
import com.ecomm.invoice.service.InvoiceService;
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
public class InvoiceEmailListener {

    private final InvoiceService invoiceService;
    private final JavaMailSender mailSender;

    @KafkaListener(
            topics = "order.placed.v1",
            groupId = "invoice-service"
    )
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for invoice generation/email: {}", event);

        try {
            // 1) Generate or fetch invoice (idempotent)
            Invoice invoice = invoiceService.createFromOrderPlaced(event);

            // 2) Build email body
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(event.getUserEmail());
            helper.setSubject("Invoice " + invoice.getInvoiceNumber()
                    + " for your order #" + event.getOrderId());

            StringBuilder body = new StringBuilder();
            body.append("Hi ").append(event.getUserId()).append(",\n\n");
            body.append("Thank you for your order #")
                    .append(event.getOrderId()).append(".\n");
            body.append("Your invoice number is ")
                    .append(invoice.getInvoiceNumber()).append(".\n\n");

            body.append("Order summary:\n");
            event.getItems().forEach(item ->
                    body.append(" - ")
                            .append(item.getProductName())
                            .append(" x ").append(item.getQuantity())
                            .append(" @ ").append(item.getPrice())
                            .append("\n")
            );

            body.append("\nTotal amount: ")
                    .append(invoice.getTotalAmount())
                    .append(" ").append(invoice.getCurrency())
                    .append("\n\n");

            body.append("If you need a copy of this invoice later, ")
                    .append("you can download it from your order history.\n\n");
            body.append("Regards,\nE-Comm Team\n");

            helper.setText(body.toString());

            // 3) Send email
            mailSender.send(message);
            log.info("Invoice email sent for order {} and invoice {}",
                    invoice.getOrderId(), invoice.getInvoiceNumber());

        } catch (Exception ex) {
            log.error("Failed to generate/send invoice email for order {}: {}",
                    event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
