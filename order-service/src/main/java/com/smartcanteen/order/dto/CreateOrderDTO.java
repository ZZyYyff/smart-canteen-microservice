package com.smartcanteen.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求 DTO
 */
@Data
public class CreateOrderDTO {

    @NotNull(message = "取餐窗口不能为空")
    private Long windowId;

    @NotEmpty(message = "至少选择一个菜品")
    private List<OrderItemDTO> items;
}
