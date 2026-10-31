package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.LoginDTO;
import com.shiguang.book.dto.RegisterDTO;
import com.shiguang.book.entity.LoginAttempt;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.LoginAttemptMapper;
import com.shiguang.book.mapper.UserMapper;
import com.shiguang.book.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAIL = 5;
    private static final int LOCK_MINUTES = 10;

    private final UserMapper userMapper;
    private final LoginAttemptMapper attemptMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> register(RegisterDTO dto) {
        // BUG-06：手机号必须先规范化，否则带空格会注册出两个「一样的」账号
        String phone = dto.getPhone().trim();
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (exists != null && exists > 0) {
            throw new BizException(409, "该手机号已注册，请直接登录");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname().trim());
        user.setDept(StringUtils.hasText(dto.getDept()) ? dto.getDept().trim() : "未填写");
        user.setAvatar("/files/default-avatar.png");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return tokenResult(user);
    }

    public Map<String, Object> login(LoginDTO dto) {
        String phone = dto.getPhone().trim();
        checkLock(phone);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            recordAttempt(phone, 0);
            throw new BizException(401, "手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "该账号已被禁用");
        }

        recordAttempt(phone, 1);
        return tokenResult(user);
    }

    public User profile(Long uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    private void checkLock(String phone) {
        Long fails = attemptMapper.selectCount(new LambdaQueryWrapper<LoginAttempt>()
                .eq(LoginAttempt::getPhone, phone)
                .eq(LoginAttempt::getSuccess, 0)
                .gt(LoginAttempt::getCreatedAt, LocalDateTime.now().minusMinutes(LOCK_MINUTES)));
        if (fails != null && fails >= MAX_FAIL) {
            throw new BizException(429, "密码错误次数过多，请 " + LOCK_MINUTES + " 分钟后再试");
        }
    }

    private void recordAttempt(String phone, int success) {
        LoginAttempt a = new LoginAttempt();
        a.setPhone(phone);
        a.setSuccess(success);
        a.setCreatedAt(LocalDateTime.now());
        attemptMapper.insert(a);
    }

    /** 登录/注册成功后统一返回 { token, user }，user 里绝不带密码 */
    private Map<String, Object> tokenResult(User user) {
        user.setPassword(null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", jwtUtil.create(user.getId()));
        data.put("user", user);
        return data;
    }
}
