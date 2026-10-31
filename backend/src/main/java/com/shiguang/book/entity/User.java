package com.shiguang.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号，唯一索引（BUG-06：入库前必须 trim） */
    private String phone;

    /** BCrypt 加密后的密码，绝不返回给前端 */
    private String password;

    private String nickname;

    private String dept;

    private String avatar;

    /** 1 正常 0 禁用 */
    private Integer status;

    private LocalDateTime createdAt;
}
