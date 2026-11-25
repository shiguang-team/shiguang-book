package com.shiguang.book.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "shiguang-book-alpha-2026-secret-key-please-change-me");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", 3600L);
    }

    @Test
    void testCreateAndParseRoundTrip() {
        String token = jwtUtil.create(1001L);
        assertNotNull(token);
        assertEquals(1001L, jwtUtil.parse(token));
        assertTrue(jwtUtil.getExpirationMillis(token) > System.currentTimeMillis());
    }

    @Test
    void testTamperedOrInvalidTokenReturnsNullAndZero() {
        String token = jwtUtil.create(1001L);
        assertNull(jwtUtil.parse(token + "tampered"));
        assertNull(jwtUtil.parse("not-a-jwt"));
        assertEquals(0L, jwtUtil.getExpirationMillis("not-a-jwt"));
    }

    @Test
    void testExpiredTokenReturnsNull() {
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", -10L);
        String expired = jwtUtil.create(1001L);
        assertNull(jwtUtil.parse(expired));
        assertEquals(0L, jwtUtil.getExpirationMillis(expired));
    }
}