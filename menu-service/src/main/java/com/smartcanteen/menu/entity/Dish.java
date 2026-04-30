package com.smartcanteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体，对应 dishes 表
 */
@Data
@TableName("dishes")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 价格 */
    private BigDecimal price;

    /** 描述 */
    private String description;

    /** 图片 URL */
    @TableField("image_url")
    private String imageUrl;

    /** 当前库存 */
    private Integer stock;

    /** 库存预警阈值，stock <= warningStock 时触发低库存预警 */
    @TableField("warning_stock")
    private Integer warningStock;

    /** 状态：ON_SALE（上架）/ OFF_SALE（下架） */
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
