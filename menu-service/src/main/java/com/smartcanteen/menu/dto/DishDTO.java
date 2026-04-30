package com.smartcanteen.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品创建/修改请求 DTO
 */
@Data
public class DishDTO {

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于 0")
    private BigDecimal price;

    /** 描述（可选） */
    private String description;

    /** 图片 URL（可选） */
    private String imageUrl;

    @NotNull(message = "库存不能为空")
    @Positive(message = "库存必须大于 0")
    private Integer stock;

    /** 库存预警阈值，默认 0 表示不预警 */
    private Integer warningStock;
}
