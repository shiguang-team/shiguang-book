package com.shiguang.book.controller;

import com.shiguang.book.common.CurrentUser;
import com.shiguang.book.common.Result;
import com.shiguang.book.entity.User;
import com.shiguang.book.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /** 个人中心：当前登录用户信息 */
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        return Result.ok(authService.profile(CurrentUser.get(request)));
    }
}
