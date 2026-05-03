package com.smartcanteen.menu.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 每日菜单 VO，包含餐段信息和关联的菜品列表
 */
@Data
@Builder
public class DailyMenuVO {

    private Long id;
    private LocalDate menuDate;
    private String mealPeriod;
    private String mealPeriodDesc;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    /** 该菜单下的菜品数量 */
    private Integer dishCount;
    /** 该菜单下的菜品列表 */
    private List<DishVO> dishes;
}
