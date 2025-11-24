package com.ecomm.invoice.web;

import com.ecomm.invoice.domain.Invoice;
import com.ecomm.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invoices", description = "Invoice lookup APIs")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Get invoice by order id")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Invoice getByOrderId(@PathVariable Long orderId) {
        return invoiceService.getByOrderId(orderId);
    }

    @Operation(summary = "Get invoice by invoice number")
    @GetMapping("/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Invoice getByInvoiceNumber(@PathVariable String invoiceNumber) {
        return invoiceService.getByInvoiceNumber(invoiceNumber);
    }
}
