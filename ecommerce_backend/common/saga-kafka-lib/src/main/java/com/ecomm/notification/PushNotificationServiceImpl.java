package com.ecomm.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void sendToUser(Long userId, String message) {
        // For now, just log. Later, integrate FCM / WebPush.
        log.info("PUSH-NOTIF → userId={} | message={}", userId, message);
    }
}
