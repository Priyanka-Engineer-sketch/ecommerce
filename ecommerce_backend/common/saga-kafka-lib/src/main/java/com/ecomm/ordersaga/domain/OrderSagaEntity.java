package com.ecomm.ordersaga.domain;

import com.ecomm.events.saga.SagaStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_saga")
public class OrderSagaEntity {

    @Id
    @Column(name = "saga_id", nullable = false, updatable = false, length = 100)
    private String sagaId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SagaStatus status;

    @Column(name = "inventory_done", nullable = false)
    private boolean inventoryDone;

    @Column(name = "payment_done", nullable = false)
    private boolean paymentDone;

    @Column(name = "shipping_done", nullable = false)
    private boolean shippingDone;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ---- getters & setters ----

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public boolean isInventoryDone() {
        return inventoryDone;
    }

    public void setInventoryDone(boolean inventoryDone) {
        this.inventoryDone = inventoryDone;
    }

    public boolean isPaymentDone() {
        return paymentDone;
    }

    public void setPaymentDone(boolean paymentDone) {
        this.paymentDone = paymentDone;
    }

    public boolean isShippingDone() {
        return shippingDone;
    }

    public void setShippingDone(boolean shippingDone) {
        this.shippingDone = shippingDone;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
