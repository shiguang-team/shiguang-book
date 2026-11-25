package com.shiguang.book.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBlacklistServiceTest {

    @Test
    void testBlacklistAndCheckValidToken() {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklistToken("jwt-token-1", System.currentTimeMillis() + 60_000);
        assertTrue(service.isBlacklisted("jwt-token-1"));
        assertEquals(1, service.size());
    }

    @Test
    void testNullOrAlreadyExpiredTokenIgnored() {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklistToken(null, System.currentTimeMillis() + 60_000);
        service.blacklistToken("old-token", System.currentTimeMillis() - 1000);

        assertFalse(service.isBlacklisted(null));
        assertFalse(service.isBlacklisted("old-token"));
        assertEquals(0, service.size());
    }

    @Test
    void testCleanExpiredTokensRemovesExpiredEntries() throws InterruptedException {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklistToken("short-lived", System.currentTimeMillis() + 30);
        assertEquals(1, service.size());

        Thread.sleep(50);
        service.cleanExpiredTokens();

        assertEquals(0, service.size());
        assertFalse(service.isBlacklisted("short-lived"));
    }
}