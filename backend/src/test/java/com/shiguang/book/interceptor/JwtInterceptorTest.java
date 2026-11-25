package com.shiguang.book.interceptor;

import com.shiguang.book.service.TokenBlacklistService;
import com.shiguang.book.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtInterceptor jwtInterceptor;

    @Test
    void testAllowsRequestWithoutAuthHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        assertTrue(jwtInterceptor.preHandle(req, resp, new Object()));
        assertNull(req.getAttribute("uid"));
    }

    @Test
    void testSetsUidAttributeForValidToken() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("good-token")).thenReturn(false);
        when(jwtUtil.parse("good-token")).thenReturn(88L);

        assertTrue(jwtInterceptor.preHandle(req, resp, new Object()));
        assertEquals(88L, req.getAttribute("uid"));
    }

    @Test
    void testBlocksBlacklistedTokenWith401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer logged-out-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("logged-out-token")).thenReturn(true);

        assertFalse(jwtInterceptor.preHandle(req, resp, new Object()));
        assertEquals(401, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("登录已注销"));
    }

    @Test
    void testBlocksExpiredOrInvalidTokenWith401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(tokenBlacklistService.isBlacklisted("bad-token")).thenReturn(false);
        when(jwtUtil.parse("bad-token")).thenReturn(null);

        assertFalse(jwtInterceptor.preHandle(req, resp, new Object()));
        assertEquals(401, resp.getStatus());
        assertTrue(resp.getContentAsString().contains("登录已过期"));
    }
}