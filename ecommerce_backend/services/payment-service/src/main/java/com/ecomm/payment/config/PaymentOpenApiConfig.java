package com.ecomm.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Service API",
                version = "v1",
                description = "Handles multi-method order payments (CARD/UPI/WALLET/COD)",
                contact = @Contact(name = "E-Comm Team", email = "support@ecomm.local")
        )
)
public class PaymentOpenApiConfig {
}
