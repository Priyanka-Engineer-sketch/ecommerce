package com.ecomm.entity.domain.fraud;

import com.ecomm.entity.User;

public interface FraudScoreService {

    FraudRiskLevel evaluateLoginRisk(User user, String ip, String userAgent);
}

