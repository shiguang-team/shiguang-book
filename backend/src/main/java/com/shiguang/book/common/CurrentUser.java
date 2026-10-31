package com.shiguang.book.common;

import jakarta.servlet.http.HttpServletRequest;

/** 从请求里取当前登录用户 id，未登录抛 401 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long get(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        if (uid == null) {
            throw new BizException(401, "请先登录");
        }
        return (Long) uid;
    }

    /** 可选登录：登录了返回 id，没登录返回 null */
    public static Long optional(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        return uid == null ? null : (Long) uid;
    }
}
