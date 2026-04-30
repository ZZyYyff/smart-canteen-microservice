package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 通用错误码枚举，统一管理系统各层返回的状态码和提示信息。
 * 业务模块自定义错误码建议从 2000 开始分段，避免与通用码冲突。
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(1001, "业务异常"),
    ORDER_STATUS_ERROR(1002, "订单状态异常"),
    SYSTEM_ERROR(500, "系统错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
