package com.smartcanteen.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录成功返回 VO，包含访问令牌、刷新令牌和用户信息
 */
@Data
@Builder
public class LoginVO {

    /** 访问令牌（JWT，24 小时有效） */
    private String token;

    /** 刷新令牌（JWT，7 天有效） */
    private String refreshToken;

    /** 用户基本信息 */
    private UserVO user;
}
