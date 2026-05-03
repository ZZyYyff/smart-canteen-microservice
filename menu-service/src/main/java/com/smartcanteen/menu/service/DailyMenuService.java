package com.smartcanteen.menu.service;

import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.vo.DailyMenuVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日菜单服务接口
 */
public interface DailyMenuService {

    /** 创建每日菜单 */
    DailyMenuVO create(DailyMenuDTO dto);

    /** 获取今日所有餐段的菜单 */
    List<DailyMenuVO> getTodayMenu();

    /** 查询每日菜单列表（支持按日期、餐次筛选） */
    List<DailyMenuVO> list(LocalDate menuDate, String mealPeriod);

    /** 编辑每日菜单 */
    DailyMenuVO update(Long id, DailyMenuDTO dto);

    /** 删除每日菜单 */
    void delete(Long id);
}
