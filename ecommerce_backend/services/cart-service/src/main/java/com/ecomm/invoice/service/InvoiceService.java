package com.ecomm.invoice.service;

import com.ecomm.events.order.OrderPlacedEvent;
import com.ecomm.invoice.domain.Invoice;
import com.ecomm.invoice.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository repo;

    public Invoice createFromOrderPlaced(OrderPlacedEvent event) {

        // idempotency: if invoice already exists, just return it
        return repo.findByOrderId(event.getOrderId())
                .orElseGet(() -> {
                    BigDecimal totalAmount = BigDecimal.valueOf(event.getTotalAmount());
                    if (totalAmount == null) {
                        totalAmount = BigDecimal.ZERO;
                    }

                    Invoice invoice = Invoice.builder()
                            .invoiceNumber("INV-" + UUID.randomUUID())
                            .orderId(event.getOrderId())
                            .userId(String.valueOf(event.getUserId()))
                            .userEmail(event.getUserEmail())
                            .totalAmount(totalAmount)
                            .currency("INR")
                            .createdAt(Instant.now())
                            .build();
                    return repo.save(invoice);
                });
    }

    @Transactional(readOnly = true)
    public Invoice getByOrderId(Long orderId) {
        return repo.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found for orderId " + orderId));
    }

    @Transactional(readOnly = true)
    public Invoice getByInvoiceNumber(String invoiceNumber) {
        return repo.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceNumber));
    }
}
