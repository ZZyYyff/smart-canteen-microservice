package com.smartcanteen.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果封装，所有 Controller 接口均通过此类包装返回数据
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    // ==================== 成功响应 ====================

    /** 操作成功，不携带数据 */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 操作成功，携带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 操作成功，自定义提示信息并携带数据 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // ==================== 失败响应 ====================

    /** 操作失败，使用默认业务异常码 1001 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(1001, message, null);
    }

    /** 操作失败，自定义状态码和提示信息 */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ==================== 兼容旧接口 ====================

    /** 操作失败，自定义状态码和提示信息（兼容 error 命名） */
    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }

    /** 操作失败，默认 500 错误（兼容 error 命名） */
    public static <T> Result<T> error(String message) {
        return fail(500, message);
    }
}
