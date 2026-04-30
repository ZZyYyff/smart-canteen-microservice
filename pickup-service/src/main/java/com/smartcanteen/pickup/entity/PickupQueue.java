package com.smartcanteen.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pickup_queue")
public class PickupQueue {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("window_id")
    private Long windowId;

    @TableField("order_id")
    private Long orderId;

    @TableField("pickup_no")
    private Integer pickupNo;

    @TableField("pickup_code")
    private String pickupCode;

    /** WAITING / CALLED / FINISHED / CANCELLED */
    private String status;

    @TableField("queue_time")
    private LocalDateTime queueTime;

    @TableField("call_time")
    private LocalDateTime callTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
