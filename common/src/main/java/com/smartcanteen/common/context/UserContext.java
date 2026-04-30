package com.smartcanteen.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户上下文信息，保存当前请求对应的用户 ID 和角色。
 * 由网关 JWT 过滤器解析 Token 后写入请求头，各微服务通过拦截器读取并存入 UserContextHolder。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    /** 用户 ID */
    private Long userId;

    /** 用户角色（STUDENT / MERCHANT / ADMIN） */
    private String role;
}
