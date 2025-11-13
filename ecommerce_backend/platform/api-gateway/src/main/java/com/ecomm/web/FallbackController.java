package com.ecomm.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping(value = "/fallback/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> fallback(@PathVariable String service) {
        return Map.of(
                "service", service,
                "status", "degraded",
                "message", "Temporarily unavailable. Please try again."
        );
    }
}
