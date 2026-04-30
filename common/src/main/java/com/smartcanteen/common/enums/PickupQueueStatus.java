package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 取餐队列状态枚举，用于大屏排队展示和取餐窗口叫号
 */
@Getter
public enum PickupQueueStatus {

    /** 排队等待中 */
    WAITING("排队中"),
    /** 已叫号，请前往取餐 */
    CALLED("已叫号"),
    /** 用户已完成取餐 */
    FINISHED("已取餐"),
    /** 该取餐号已取消 */
    CANCELLED("已取消");

    private final String description;

    PickupQueueStatus(String description) {
        this.description = description;
    }
}
