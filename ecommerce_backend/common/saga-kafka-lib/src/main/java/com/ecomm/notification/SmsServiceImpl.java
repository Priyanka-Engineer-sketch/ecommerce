package com.ecomm.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String phone, String message) {
        // For now, just log. Later, integrate with a real SMS gateway.
        log.info("SMS → phone={} | message={}", phone, message);
    }
}
