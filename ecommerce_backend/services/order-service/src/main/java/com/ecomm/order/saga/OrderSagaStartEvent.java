package com.ecomm.order.saga;

import com.ecomm.events.order.OrderItemPayload;
import com.ecomm.events.saga.SagaStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderSagaStartEvent {
    private String sagaId;
    private String orderId;
    private String customerId;
    private double totalAmount;
    private SagaStatus status;
    private List<OrderItemPayload> items;
}
