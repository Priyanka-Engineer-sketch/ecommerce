package com.ecomm.cart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cart Service API",
                version = "v1",
                description = "Manages shopping carts per user before checkout",
                contact = @Contact(name = "E-Comm Team", email = "support@ecomm.local")
        )
)
public class CartOpenApiConfig {
}
