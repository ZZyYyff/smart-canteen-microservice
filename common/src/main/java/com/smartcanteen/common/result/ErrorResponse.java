package com.smartcanteen.common.result;

import lombok.Builder;
import lombok.Data;

/**
 * 全局异常响应结构，用于统一格式化异常信息返回给前端。
 * 与 Result 的区别：ErrorResponse 额外携带 timestamp、path、detail 等调试字段。
 */
@Data
@Builder
public class ErrorResponse {

    /** 错误码 */
    private Integer code;

    /** 错误提示 */
    private String message;

    /** 详细错误描述（可选，生产环境建议置空） */
    private String detail;

    /** 错误发生时间戳（毫秒） */
    private Long timestamp;

    /** 请求路径（可选） */
    private String path;

    /** 快速构建只含 code 和 message 的错误响应 */
    public static ErrorResponse of(Integer code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 构建包含详细描述的错误响应 */
    public static ErrorResponse of(Integer code, String message, String detail) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .detail(detail)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
