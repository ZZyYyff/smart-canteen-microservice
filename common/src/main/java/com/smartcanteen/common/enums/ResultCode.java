package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 通用响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    PASSWORD_ERROR(2002, "密码错误"),
    TOKEN_EXPIRED(2003, "Token 已过期"),
    USERNAME_DUPLICATE(2004, "用户名已存在"),

    // 菜单模块 3xxx
    MENU_NOT_FOUND(3001, "菜品不存在"),
    STOCK_INSUFFICIENT(3002, "库存不足"),

    // 订单模块 4xxx
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态异常"),
    ORDER_CANNOT_CANCEL(4003, "当前订单状态不允许取消"),

    // 取餐模块 5xxx
    PICKUP_CODE_ERROR(5001, "取餐码错误"),
    PICKUP_ALREADY_COMPLETED(5002, "该订单已取餐"),
    QUEUE_EMPTY(5003, "取餐队列为空");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
