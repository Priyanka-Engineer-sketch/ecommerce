package com.ecomm.apigateway.filter;

import lombok.Data;
import java.util.List;

@Data
public class JwtAuthConfig {
    private boolean required = true;
    private List<String> roles; // allowed roles, or null for any
}
