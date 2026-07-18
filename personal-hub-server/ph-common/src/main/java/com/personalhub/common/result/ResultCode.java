package com.personalhub.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回状态码枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "返回状态码")
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 客户端请求错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未认证 / Token 过期
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    SERVER_ERROR(500, "服务器内部错误");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 提示信息
     */
    private final String message;
}
