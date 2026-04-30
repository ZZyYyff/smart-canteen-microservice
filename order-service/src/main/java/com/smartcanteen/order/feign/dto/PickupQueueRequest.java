package com.smartcanteen.order.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加入取餐队列请求（与 pickup-service 对应）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupQueueRequest {

    private Long orderId;
    private Long userId;
    private Long windowId;
    private Integer pickupNo;
    private String pickupCode;
}
