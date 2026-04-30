package com.smartcanteen.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcanteen.menu.entity.DailyMenuItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日菜单关联菜品 Mapper
 */
@Mapper
public interface DailyMenuItemMapper extends BaseMapper<DailyMenuItem> {
}
