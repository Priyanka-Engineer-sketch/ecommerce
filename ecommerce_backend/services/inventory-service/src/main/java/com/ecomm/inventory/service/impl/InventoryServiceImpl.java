package com.ecomm.inventory.service.impl;

import com.ecomm.inventory.domain.InventoryItem;
import com.ecomm.inventory.dto.request.InventoryAdjustmentRequest;
import com.ecomm.inventory.dto.response.InventoryResponse;
import com.ecomm.inventory.repository.InventoryRepository;
import com.ecomm.inventory.service.InventoryService;
import com.ecomm.inventory.service.InventoryUpdatePublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repo;
    private final InventoryUpdatePublisher updatePublisher;

    // ----------------- READ (CACHED via Redis) -----------------
    @Override
    @Cacheable(cacheNames = "inventory", key = "#sku")
    public InventoryResponse getBySku(String sku) {
        InventoryItem item = repo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("SKU not found: " + sku));
        return new InventoryResponse(item.getSku(), item.getAvailable(), item.getReserved());
    }

    // ----------------- ADMIN / SELLER STOCK ADJUSTMENT -----------------
    @Override
    @Transactional
    @CacheEvict(cacheNames = "inventory", key = "#request.sku()")
    public InventoryResponse adjustStock(InventoryAdjustmentRequest request, String actorUserId) {
        InventoryItem item = repo.findBySku(request.sku())
                .orElseThrow(() -> new RuntimeException("SKU not found: " + request.sku()));

        long newAvailable = item.getAvailable() + request.dataAvailable();
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Resulting stock cannot be negative");
        }

        item.setAvailable(newAvailable);
        repo.save(item);

        InventoryResponse response = new InventoryResponse(item.getSku(), item.getAvailable(), item.getReserved());

        // Notify dashboards via WebSocket asynchronously
        notifyAsync(response);

        log.info("Inventory adjusted by user {} for sku {}: delta={}, newAvailable={}",
                actorUserId, item.getSku(), request.dataAvailable(), newAvailable);

        return response;
    }

    // WebSocket notification using CompletableFuture/@Async style
    @Async
    protected void notifyAsync(InventoryResponse response) {
        updatePublisher.publishUpdate(response);
    }

    // ----------------- SAGA: RESERVE STOCK --- (called by Kafka handler) ----
    @Override
    @Transactional
    public boolean reserveForOrder(Long orderId, Object payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) payload;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");

            // Validation pass
            for (Map<String, Object> line : items) {
                String sku = (String) line.get("sku");
                int qty = ((Number) line.get("quantity")).intValue();

                InventoryItem item = repo.findBySku(sku)
                        .orElseThrow(() -> new RuntimeException("Unknown SKU: " + sku));
                if (item.getAvailable() < qty) {
                    log.warn("Insufficient stock for order {} sku {}: requested={}, available={}",
                            orderId, sku, qty, item.getAvailable());
                    return false;
                }
            }

            // Reserve pass
            for (Map<String, Object> line : items) {
                String sku = (String) line.get("sku");
                int qty = ((Number) line.get("quantity")).intValue();

                InventoryItem item = repo.findBySku(sku).orElseThrow();
                item.setAvailable(item.getAvailable() - qty);
                item.setReserved(item.getReserved() + qty);
                repo.save(item);

                // evict cache & notify websocket
                InventoryResponse response = new InventoryResponse(item.getSku(), item.getAvailable(), item.getReserved());
                updatePublisher.publishUpdate(response);
            }

            log.info("Inventory reserved for order {}", orderId);
            return true;

        } catch (Exception e) {
            log.error("Error reserving stock for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }

    // For compensation
    @Override
    @Transactional
    public void releaseForOrder(Long orderId) {
        log.info("Releasing stock for order {} (compensation TODO: implement with audit)", orderId);
        // TODO: Track order-item reservations to reverse them here
    }
}
