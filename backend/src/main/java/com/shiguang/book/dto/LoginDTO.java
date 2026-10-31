package com.shiguang.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 图形验证码，开发环境传 123456 或不传 */
    private String captcha;
}
