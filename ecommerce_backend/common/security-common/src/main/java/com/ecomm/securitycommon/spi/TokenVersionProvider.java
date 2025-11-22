package com.ecomm.securitycommon.spi;

/**
 * Implemented by user-service (or any auth service) to provide
 * current token-version stored in DB / cache.
 */
public interface TokenVersionProvider {
    /**
     * @return current version for this user, or null if not found
     */
    Integer currentTokenVersion(String username);
}