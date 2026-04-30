package com.smartcanteen.user.service;

import com.smartcanteen.user.dto.LoginDTO;
import com.smartcanteen.user.dto.RegisterDTO;
import com.smartcanteen.user.dto.UpdateUserDTO;
import com.smartcanteen.user.vo.LoginVO;
import com.smartcanteen.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /** 用户注册 */
    UserVO register(RegisterDTO dto);

    /** 用户登录 */
    LoginVO login(LoginDTO dto);

    /** 刷新令牌 */
    LoginVO refreshToken(String refreshToken);

    /** 查询当前登录用户信息 */
    UserVO getCurrentUser(Long userId);

    /** 修改当前用户信息 */
    UserVO updateCurrentUser(Long userId, UpdateUserDTO dto);
}
