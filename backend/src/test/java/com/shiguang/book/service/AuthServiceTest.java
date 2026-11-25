package com.shiguang.book.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.shiguang.book.common.BizException;
import com.shiguang.book.dto.LoginDTO;
import com.shiguang.book.dto.RegisterDTO;
import com.shiguang.book.entity.LoginAttempt;
import com.shiguang.book.entity.User;
import com.shiguang.book.mapper.LoginAttemptMapper;
import com.shiguang.book.mapper.UserMapper;
import com.shiguang.book.util.JwtUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginAttemptMapper attemptMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeAll
    static void initMybatisCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, LoginAttempt.class);
    }

    @Test
    void testRegisterTrimsPhoneHashesPasswordAndReturnsToken() {
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone(" 13800000001 ");
        dto.setPassword("123456");
        dto.setNickname(" 张同学 ");
        dto.setDept("");

        AtomicReference<String> persistedPhone = new AtomicReference<>();
        AtomicReference<String> persistedHash = new AtomicReference<>();
        AtomicReference<String> persistedDept = new AtomicReference<>();

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            persistedPhone.set(u.getPhone());
            persistedHash.set(u.getPassword());
            persistedDept.set(u.getDept());
            return 1;
        });
        when(jwtUtil.create(10L)).thenReturn("mock-jwt-token");

        Map<String, Object> res = authService.register(dto);

        assertEquals("13800000001", persistedPhone.get());
        assertEquals("未填写", persistedDept.get());
        assertTrue(new BCryptPasswordEncoder().matches("123456", persistedHash.get()));
        assertEquals("mock-jwt-token", res.get("token"));

        User returnedUser = (User) res.get("user");
        assertNotNull(returnedUser);
        assertNull(returnedUser.getPassword());
    }

    @Test
    void testRegisterDuplicatePhoneThrows409() {
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone("13800000001");
        dto.setPassword("123456");
        dto.setNickname("重复用户");

        when(userMapper.selectCount(any())).thenReturn(1L);

        BizException ex = assertThrows(BizException.class, () -> authService.register(dto));
        assertEquals(409, ex.getCode());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLoginSuccessRecordsAttemptAndReturnsToken() {
        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setPhone("13800000001");
        dbUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
        dbUser.setStatus(1);

        when(attemptMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectOne(any())).thenReturn(dbUser);
        when(jwtUtil.create(1L)).thenReturn("valid-jwt");

        LoginDTO dto = new LoginDTO();
        dto.setPhone(" 13800000001 ");
        dto.setPassword("123456");

        Map<String, Object> res = authService.login(dto);

        assertEquals("valid-jwt", res.get("token"));
        assertNull(((User) res.get("user")).getPassword());

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(attemptMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getSuccess());
    }

    @Test
    void testLoginWrongPasswordRecordsFailureAndThrows401() {
        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setPhone("13800000001");
        dbUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
        dbUser.setStatus(1);

        when(attemptMapper.selectCount(any())).thenReturn(2L);
        when(userMapper.selectOne(any())).thenReturn(dbUser);

        LoginDTO dto = new LoginDTO();
        dto.setPhone("13800000001");
        dto.setPassword("wrong-pwd");

        BizException ex = assertThrows(BizException.class, () -> authService.login(dto));
        assertEquals(401, ex.getCode());

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(attemptMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getSuccess());
    }

    @Test
    void testLoginLockedAfterFiveFailuresThrows429() {
        when(attemptMapper.selectCount(any())).thenReturn(5L);

        LoginDTO dto = new LoginDTO();
        dto.setPhone("13800000001");
        dto.setPassword("123456");

        BizException ex = assertThrows(BizException.class, () -> authService.login(dto));
        assertEquals(429, ex.getCode());
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void testLoginDisabledAccountThrows403() {
        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setPhone("13800000001");
        dbUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
        dbUser.setStatus(0);

        when(attemptMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectOne(any())).thenReturn(dbUser);

        LoginDTO dto = new LoginDTO();
        dto.setPhone("13800000001");
        dto.setPassword("123456");

        BizException ex = assertThrows(BizException.class, () -> authService.login(dto));
        assertEquals(403, ex.getCode());
    }

    @Test
    void testProfileReturnsSanitizedUserAndThrows404WhenMissing() {
        User dbUser = new User();
        dbUser.setId(2L);
        dbUser.setNickname("小李");
        dbUser.setPassword("secret-hash");

        when(userMapper.selectById(2L)).thenReturn(dbUser);
        when(userMapper.selectById(999L)).thenReturn(null);

        User profile = authService.profile(2L);
        assertEquals("小李", profile.getNickname());
        assertNull(profile.getPassword());

        BizException ex = assertThrows(BizException.class, () -> authService.profile(999L));
        assertEquals(404, ex.getCode());
    }
}