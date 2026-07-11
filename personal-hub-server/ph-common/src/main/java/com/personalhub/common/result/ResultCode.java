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

    /** 成功 */
    SUCCESS(200, "success"),
    /** 客户端请求错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未认证 / Token 过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无权限 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    /** 请求超时 */
    REQUEST_TIMEOUT(408, "请求超时"),
    /** 参数校验失败 */
    VALIDATION_FAILED(422, "参数校验失败"),
    /** 服务器内部错误 */
    SERVER_ERROR(500, "服务器内部错误"),
    /** 服务不可用 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    ;

    /** 状态码 */
    private final int code;
    /** 提示信息 */
    private final String message;

    /**
     * 根据 code 查找枚举
     *
     * @param code 状态码
     * @return 匹配的枚举，找不到返回 null
     */
    public static ResultCode fromCode(int code) {
        for (ResultCode rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return null;
    }
}
