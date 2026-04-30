package com.smartcanteen.common.exception;

import com.smartcanteen.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常，在 Service 层抛出后由全局异常处理器捕获并转换为 Result 返回。
 * 与 ErrorCode 枚举配合使用，统一管理错误码和提示信息。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    /**
     * 使用 ErrorCode 枚举创建异常，提示信息取自枚举值
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用 ErrorCode 枚举创建异常，自定义提示信息覆盖枚举默认值
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 自定义错误码和提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 默认 500 服务器错误
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
