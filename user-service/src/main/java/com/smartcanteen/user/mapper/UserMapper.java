package com.smartcanteen.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcanteen.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper，继承 MyBatis Plus BaseMapper 获得通用 CRUD 能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
