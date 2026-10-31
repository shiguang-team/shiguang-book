package com.shiguang.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 登录失败记录表，用于「连续 5 次密码错误锁 10 分钟」 */
@Data
@TableName("login_attempt")
public class LoginAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    /** 1 成功 0 失败 */
    private Integer success;

    private LocalDateTime createdAt;
}
