package com.ecomm.events.inventory;

import com.ecomm.events.order.SagaStep;
import lombok.Data;

@Data
public class InventoryResultEvent {
    private String sagaId;
    private Long orderId;
    private boolean success;
    private String message;
    private SagaStep step; // should be SagaStep.INVENTORY
}
