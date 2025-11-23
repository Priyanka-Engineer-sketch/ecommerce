package com.ecomm.reco.service.impl;

import com.ecomm.reco.service.UserActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserActivityServiceImpl implements UserActivityService {

    @Override
    public void registerLogin(Long userId, String ip, String userAgent, long timestamp) {
        log.info("RECO → User login registered: userId={} ip={} agent={} ts={}",
                userId, ip, userAgent, timestamp);
    }
}
