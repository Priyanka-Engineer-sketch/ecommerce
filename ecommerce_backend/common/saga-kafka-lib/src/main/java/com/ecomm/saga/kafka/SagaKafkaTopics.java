package com.ecomm.saga.kafka;

public final class SagaKafkaTopics {

    private SagaKafkaTopics() {}

    public static final String ORDER_SAGA_START           = "order.saga.start";
    public static final String ORDER_SAGA_CMD_INVENTORY   = "order.saga.commands.inventory";
    public static final String ORDER_SAGA_CMD_PAYMENT     = "order.saga.commands.payment";
    public static final String ORDER_SAGA_CMD_SHIPPING    = "order.saga.commands.shipping";
    public static final String ORDER_SAGA_REPLIES         = "order.saga.replies";

    // ---------- NEW USER TOPICS ----------
    public static final String USER_REGISTERED       = "user.registered.v1";
    public static final String USER_LOGIN            = "user.login.v1";
    public static final String USER_FRAUD_ALERT      = "user.fraud.alert.v1";
    public static final String USER_PASSWORD_RESET   = "user.password.reset.v1";
    public static final String USER_EMAIL_OUTBOX     = "user.email.outbox.v1";
}
