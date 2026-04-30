package com.smartcanteen.menu.service;

import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.vo.DailyMenuVO;

import java.util.List;

/**
 * 每日菜单服务接口
 */
public interface DailyMenuService {

    /** 创建每日菜单 */
    DailyMenuVO create(DailyMenuDTO dto);

    /** 获取今日所有餐段的菜单 */
    List<DailyMenuVO> getTodayMenu();
}
