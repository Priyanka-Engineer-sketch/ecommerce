package com.ecomm.configserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigServerSecurityConfig {

    @Bean
    public SecurityFilterChain configServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // <-- allow everything
                )
                .formLogin(form -> form.disable())  // no login page
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
