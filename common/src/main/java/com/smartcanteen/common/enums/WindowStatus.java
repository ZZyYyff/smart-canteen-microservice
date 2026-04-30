package com.smartcanteen.common.enums;

import lombok.Getter;

@Getter
public enum WindowStatus {

    ACTIVE("启用"),
    DISABLED("停用");

    private final String description;

    WindowStatus(String description) {
        this.description = description;
    }
}
