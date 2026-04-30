package com.smartcanteen.pickup.vo;

import com.smartcanteen.common.enums.PickupQueueStatus;
import com.smartcanteen.pickup.entity.PickupQueue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickupQueueVO {

    private Long id;
    private Long windowId;
    private String windowName;
    private Long orderId;
    private Integer pickupNo;
    private String status;
    private String statusDesc;
    private LocalDateTime queueTime;
    private LocalDateTime callTime;
    private LocalDateTime finishTime;

    public static PickupQueueVO fromEntity(PickupQueue q) {
        PickupQueueVO vo = new PickupQueueVO();
        vo.setId(q.getId());
        vo.setWindowId(q.getWindowId());
        vo.setOrderId(q.getOrderId());
        vo.setPickupNo(q.getPickupNo());
        vo.setStatus(q.getStatus());
        vo.setQueueTime(q.getQueueTime());
        vo.setCallTime(q.getCallTime());
        vo.setFinishTime(q.getFinishTime());
        try {
            vo.setStatusDesc(PickupQueueStatus.valueOf(q.getStatus()).getDescription());
        } catch (IllegalArgumentException e) {
            vo.setStatusDesc(q.getStatus());
        }
        return vo;
    }
}
