package com.smartcanteen.pickup.dto;

import lombok.Data;

@Data
public class AddToQueueRequest {

    private Long orderId;
    private Long userId;
    private Long windowId;
    private Integer pickupNo;
    private String pickupCode;
}
