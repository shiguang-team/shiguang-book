package com.shiguang.book.common;

import lombok.Getter;

/**
 * 业务异常。错误码约定：
 * 400 参数错误 / 401 未登录或登录过期 / 403 无权限 / 404 资源不存在 / 409 状态冲突
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(400, message);
    }
}
