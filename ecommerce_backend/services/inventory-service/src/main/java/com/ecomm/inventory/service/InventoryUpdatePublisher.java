package com.ecomm.inventory.service;

import com.ecomm.inventory.dto.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryUpdatePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pushes live stock updates to WebSocket subscribers.
     * Clients subscribe to:
     *     /topic/inventory/{sku}
     */
    public void publishUpdate(InventoryResponse response) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/inventory/" + response.sku(),
                    response
            );
            log.info("Pushed WS inventory update for SKU {}: {}", response.sku(), response);
        } catch (Exception e) {
            log.error("Failed to send WebSocket inventory update for {}: {}", response.sku(), e.getMessage());
        }
    }
}
