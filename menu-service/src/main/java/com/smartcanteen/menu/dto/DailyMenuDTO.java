package com.smartcanteen.menu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 每日菜单创建请求 DTO
 */
@Data
public class DailyMenuDTO {

    @NotNull(message = "菜单日期不能为空")
    private LocalDate menuDate;

    @NotNull(message = "餐段不能为空")
    private String mealPeriod;

    @NotNull(message = "售卖开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "售卖结束时间不能为空")
    private LocalTime endTime;

    @NotEmpty(message = "至少选择一个菜品")
    private List<Long> dishIds;
}
