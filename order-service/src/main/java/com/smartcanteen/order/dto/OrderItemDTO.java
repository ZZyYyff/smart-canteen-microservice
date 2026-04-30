package com.smartcanteen.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单菜品项 DTO
 */
@Data
public class OrderItemDTO {

    @NotNull(message = "菜品 ID 不能为空")
    private Long dishId;

    @NotBlank(message = "菜品名称不能为空")
    private String dishName;

    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于 0")
    private BigDecimal price;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;
}
