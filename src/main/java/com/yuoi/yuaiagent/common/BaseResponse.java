package com.yuoi.yuaiagent.common;

/**
 * 统一接口响应封装（扩展：完善项目健壮性）。
 */
public record BaseResponse<T>(int code, T data, String message) {

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}
