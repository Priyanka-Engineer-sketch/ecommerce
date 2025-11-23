package com.ecomm.saga.kafka;

public final class SagaKafkaTopics {

    private SagaKafkaTopics() {}

    // ================================
    // ORDER SAGA TOPICS
    // ================================
    public static final String ORDER_SAGA_START          = "order.saga.start.v1";
    public static final String ORDER_SAGA_CMD_INVENTORY  = "order.saga.commands.inventory.v1";
    public static final String ORDER_SAGA_CMD_PAYMENT    = "order.saga.commands.payment.v1";
    public static final String ORDER_SAGA_CMD_SHIPPING   = "order.saga.commands.shipping.v1";
    public static final String ORDER_SAGA_REPLIES        = "order.saga.replies.v1";

    // ================================
    // USER EVENTS
    // ================================
    public static final String USER_REGISTERED           = "user.registered.v1";
    public static final String USER_REGISTERED_BY_ADMIN  = "user.registered.by.admin.v1";

    public static final String USER_LOGIN_SUCCESS        = "user.login.success.v1";
    public static final String USER_LOGIN_SUSPICIOUS     = "user.login.suspicious.v1";
    public static final String USER_LOGIN_FRAUD          = "user.login.fraud.v1";

    public static final String USER_PASSWORD_RESET       = "user.password.reset.v1";
    public static final String USER_EMAIL_VERIFIED       = "user.email.verified.v1";

    public static final String USER_REFRESH_TOKEN        = "user.refresh.token.v1";
    public static final String USER_LOGOUT_ALL           = "user.logout.all.v1";

    public static final String USER_EMAIL_OUTBOX         = "user.email.outbox.v1";

    public static final String USER_FRAUD_ALERT          = "user.fraud.alert";
}
