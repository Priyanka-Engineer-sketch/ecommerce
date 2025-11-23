package com.ecomm.notification;

/**
 * Abstraction for sending SMS messages.
 * Replace with real provider (Twilio, AWS SNS, etc.) later.
 */
public interface SmsService {

    /**
     * Send SMS to a phone number.
     */
    void sendSms(String phone, String message);
}
