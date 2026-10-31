package com.shiguang.book.controller;

import com.shiguang.book.common.Result;
import com.shiguang.book.dto.LoginDTO;
import com.shiguang.book.dto.RegisterDTO;
import com.shiguang.book.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${shiguang.captcha.dev-fixed-code:123456}")
    private String fixedCode;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /**
     * 开发环境的验证码：固定码 123456，同时打到日志里方便测试同学。
     * 生产环境应替换成图形验证码服务（第 7 周排期）。
     */
    @GetMapping("/captcha")
    public Result<Map<String, Object>> captcha() {
        String key = UUID.randomUUID().toString().replace("-", "");
        log.info("[DEV] 验证码 key={}, code={}", key, fixedCode);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("code", fixedCode);
        return Result.ok(data);
    }
}
