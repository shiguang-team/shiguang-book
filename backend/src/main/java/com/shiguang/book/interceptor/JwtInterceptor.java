package com.shiguang.book.interceptor;

import com.shiguang.book.service.TokenBlacklistService;
import com.shiguang.book.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 解析请求头中的 Bearer Token（若存在）并校验黑名单与有效期；
 * 需要强制登录的接口在 Controller 中通过 CurrentUser.get(request) 获取用户 ID。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenBlacklistService.isBlacklisted(token)) {
                writeError(response, "登录已注销，请重新登录");
                return false;
            }
            Long uid = jwtUtil.parse(token);
            if (uid == null) {
                writeError(response, "登录已过期，请重新登录");
                return false;
            }
            request.setAttribute("uid", uid);
        }
        return true;
    }

    private void writeError(HttpServletResponse response, String message) {
        try {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
        } catch (Exception e) {
            log.warn("写入 401 未授权响应失败", e);
        }
    }
}