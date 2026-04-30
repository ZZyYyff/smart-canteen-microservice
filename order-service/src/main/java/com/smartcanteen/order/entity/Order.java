package com.smartcanteen.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应 orders 表
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 下单用户 ID */
    @TableField("user_id")
    private Long userId;

    /** 取餐窗口 ID */
    @TableField("window_id")
    private Long windowId;

    /** 订单总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 订单状态：CREATED / ACCEPTED / COOKING / WAIT_PICKUP / COMPLETED / CANCELLED */
    private String status;

    /** 取餐号（排队序号） */
    @TableField("pickup_no")
    private Integer pickupNo;

    /** 取餐码（核销验证码） */
    @TableField("pickup_code")
    private String pickupCode;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
