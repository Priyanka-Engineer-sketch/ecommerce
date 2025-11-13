package com.ecomm.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class SecurityRedisConfig  {
    @Bean
    public StringRedisTemplate stringRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
