package com.ecomm.config.security;

import java.util.Set;

public record AuthUser(Long id, String email, Set<String> roles) { }


