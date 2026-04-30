package com.smartcanteen.menu.enums;

import lombok.Getter;

/**
 * 餐段枚举
 */
@Getter
public enum MealPeriod {

    BREAKFAST("早餐"),
    LUNCH("午餐"),
    DINNER("晚餐");

    private final String description;

    MealPeriod(String description) {
        this.description = description;
    }
}
