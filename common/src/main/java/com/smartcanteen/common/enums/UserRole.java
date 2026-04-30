package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    STUDENT("学生"),
    MERCHANT("商家"),
    ADMIN("管理员");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
