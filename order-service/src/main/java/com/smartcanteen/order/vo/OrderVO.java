package com.smartcanteen.order.vo;

import com.smartcanteen.common.enums.OrderStatus;
import com.smartcanteen.order.entity.Order;
import com.smartcanteen.order.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO
 */
@Data
@Builder
public class OrderVO {

    private Long id;
    private Long userId;
    private Long windowId;
    private BigDecimal totalAmount;
    private String status;
    private String statusDesc;
    private Integer pickupNo;
    private String pickupCode;
    private List<OrderItemVO> items;
    private LocalDateTime createdAt;

    /** 从实体组装 VO */
    public static OrderVO fromEntity(Order order, List<OrderItem> items) {
        String status = order.getStatus();
        String statusDesc = "";
        try {
            statusDesc = OrderStatus.valueOf(status).getDescription();
        } catch (IllegalArgumentException ignored) {
        }

        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> OrderItemVO.builder()
                        .dishId(item.getDishId())
                        .dishName(item.getDishName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return OrderVO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .windowId(order.getWindowId())
                .totalAmount(order.getTotalAmount())
                .status(status)
                .statusDesc(statusDesc)
                .pickupNo(order.getPickupNo())
                .pickupCode(order.getPickupCode())
                .items(itemVOs)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
