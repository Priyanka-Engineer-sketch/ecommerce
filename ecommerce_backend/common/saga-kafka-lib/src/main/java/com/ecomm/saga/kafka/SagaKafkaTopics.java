package com.ecomm.saga.kafka;

public final class SagaKafkaTopics {

    private SagaKafkaTopics() {}

    public static final String ORDER_SAGA_START           = "order.saga.start";
    public static final String ORDER_SAGA_CMD_INVENTORY   = "order.saga.commands.inventory";
    public static final String ORDER_SAGA_CMD_PAYMENT     = "order.saga.commands.payment";
    public static final String ORDER_SAGA_CMD_SHIPPING    = "order.saga.commands.shipping";
    public static final String ORDER_SAGA_REPLIES         = "order.saga.replies";
}
