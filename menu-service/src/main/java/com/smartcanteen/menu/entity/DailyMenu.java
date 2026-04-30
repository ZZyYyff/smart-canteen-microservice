package com.smartcanteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 每日菜单实体，对应 daily_menus 表
 */
@Data
@TableName("daily_menus")
public class DailyMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜单日期 */
    @TableField("menu_date")
    private LocalDate menuDate;

    /** 餐段：BREAKFAST / LUNCH / DINNER */
    @TableField("meal_period")
    private String mealPeriod;

    /** 售卖开始时间 */
    @TableField("start_time")
    private LocalTime startTime;

    /** 售卖结束时间 */
    @TableField("end_time")
    private LocalTime endTime;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
