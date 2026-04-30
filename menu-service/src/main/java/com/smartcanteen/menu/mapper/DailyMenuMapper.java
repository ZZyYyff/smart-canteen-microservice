package com.smartcanteen.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcanteen.menu.entity.DailyMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日菜单 Mapper
 */
@Mapper
public interface DailyMenuMapper extends BaseMapper<DailyMenu> {
}
