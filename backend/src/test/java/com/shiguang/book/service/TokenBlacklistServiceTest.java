package com.shiguang.book.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TokenBlacklistServiceTest {
    @Test
    void testTokenBlacklistAndExpirationClean() {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklistToken("jwt-token-1", System.currentTimeMillis() + 60_000);
        assertTrue(service.isBlacklisted("jwt-token-1"));
    }
}
