package com.personalhub.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一返回结果
 */
@Data
@Schema(description = "统一返回结果")
public class Result<T> {

    @Schema(description = "状态码", example = "200")
    private int code;

    @Schema(description = "提示信息", example = "success")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    private Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功 ==========

    public static <T> Result<T> success() {
        return of(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return of(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ========== 失败 ==========

    public static <T> Result<T> error(ResultCode resultCode) {
        return of(resultCode, null);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 快捷：参数校验失败
     */
    public static <T> Result<T> badRequest(String message) {
        return error(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 快捷：未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 快捷：资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return error(ResultCode.NOT_FOUND, message);
    }

    /**
     * 快捷：服务器内部错误
     */
    public static <T> Result<T> serverError(String message) {
        return error(ResultCode.SERVER_ERROR, message);
    }

    private static <T> Result<T> of(ResultCode rc, T data) {
        return new Result<>(rc.getCode(), rc.getMessage(), data);
    }
}
