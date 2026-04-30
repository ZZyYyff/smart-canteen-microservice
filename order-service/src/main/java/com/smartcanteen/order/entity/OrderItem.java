package com.smartcanteen.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体，对应 order_items 表
 */
@Data
@TableName("order_items")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单 ID */
    @TableField("order_id")
    private Long orderId;

    /** 菜品 ID */
    @TableField("dish_id")
    private Long dishId;

    /** 菜品名称（快照） */
    @TableField("dish_name")
    private String dishName;

    /** 下单时单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;
}
