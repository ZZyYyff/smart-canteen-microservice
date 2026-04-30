package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 菜品上架/下架状态枚举
 */
@Getter
public enum DishStatus {

    ON_SALE("上架中"),
    OFF_SALE("已下架");

    private final String description;

    DishStatus(String description) {
        this.description = description;
    }
}
