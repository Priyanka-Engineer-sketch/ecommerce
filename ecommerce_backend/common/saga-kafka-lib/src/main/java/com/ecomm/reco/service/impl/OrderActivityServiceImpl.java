package com.ecomm.reco.service.impl;

import com.ecomm.reco.service.OrderActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderActivityServiceImpl implements OrderActivityService {

    @Override
    public void orderCompleted(Long orderId) {
        // For now just log. Later, fetch order lines and update
        // user-item interaction tables or ML features.
        log.info("RECOMMENDER → orderCompleted(orderId={}) - updating recommendation model", orderId);
    }
}
