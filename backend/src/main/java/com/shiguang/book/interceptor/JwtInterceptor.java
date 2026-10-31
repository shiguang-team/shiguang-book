package com.shiguang.book.interceptor;

import com.shiguang.book.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 只做「有 token 就解析」这一件事，不在这里拦人。
 * 需要登录的接口在 Controller 里用 CurrentUser.get(request) 拿用户，拿不到就抛 401。
 * 这样 GET 的公开接口（首页、详情）不需要在路径上做白名单，避免 BUG-01 那种守卫死循环。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long uid = jwtUtil.parse(header.substring(7));
            if (uid == null) {
                // token 过期 / 伪造：明确告诉前端，让它清掉本地 token 重新登录
                writeError(response);
                return false;
            }
            request.setAttribute("uid", uid);
        }
        return true;
    }

    private void writeError(HttpServletResponse response) {
        try {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
        } catch (Exception ignored) {
        }
    }
}
