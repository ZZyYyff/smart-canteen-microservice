package com.smartcanteen.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举，覆盖从下单到取餐完成的完整生命周期
 */
@Getter
public enum OrderStatus {

    /** 用户已下单，等待商家接单 */
    CREATED("已创建"),
    /** 商家已确认接单 */
    ACCEPTED("商家已接单"),
    /** 商家正在备餐 */
    COOKING("制作中"),
    /** 备餐完成，等待用户取餐 */
    WAIT_PICKUP("待取餐"),
    /** 用户已取餐，订单完成 */
    COMPLETED("已完成"),
    /** 订单已取消（仅 CREATED 状态可取消） */
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
