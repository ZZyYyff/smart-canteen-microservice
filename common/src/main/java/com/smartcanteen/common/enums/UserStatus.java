package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {

    NORMAL("正常"),
    DISABLED("已禁用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
