package com.smartcanteen.menu.vo;

import com.smartcanteen.common.enums.DishStatus;
import com.smartcanteen.menu.entity.Dish;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品信息 VO，额外携带状态描述和低库存预警标识
 */
@Data
@Builder
public class DishVO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Integer warningStock;
    private String status;
    private String statusDesc;
    /** 是否低库存预警 */
    private Boolean lowStock;
    private LocalDateTime createdAt;

    /** 从实体构建 VO */
    public static DishVO fromEntity(Dish dish) {
        if (dish == null) {
            return null;
        }
        String status = dish.getStatus();
        boolean lowStock = dish.getStock() != null
                && dish.getWarningStock() != null
                && dish.getWarningStock() > 0
                && dish.getStock() <= dish.getWarningStock();

        return DishVO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .description(dish.getDescription())
                .imageUrl(dish.getImageUrl())
                .stock(dish.getStock())
                .warningStock(dish.getWarningStock())
                .status(status)
                .statusDesc("ON_SALE".equals(status) ? DishStatus.ON_SALE.getDescription()
                        : DishStatus.OFF_SALE.getDescription())
                .lowStock(lowStock)
                .createdAt(dish.getCreatedAt())
                .build();
    }
}
