package com.smartcanteen.order.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细 VO
 */
@Data
@Builder
public class OrderItemVO {

    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private Integer quantity;
}
