package com.smartcanteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 每日菜单关联菜品实体，对应 daily_menu_items 表
 */
@Data
@TableName("daily_menu_items")
public class DailyMenuItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜单 ID */
    @TableField("menu_id")
    private Long menuId;

    /** 菜品 ID */
    @TableField("dish_id")
    private Long dishId;
}
