package com.smartcanteen.user.config;

import com.smartcanteen.common.context.UserContext;
import com.smartcanteen.common.context.UserContextHolder;
import com.smartcanteen.common.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器，从请求头中提取用户信息并存入 ThreadLocal。
 * 优先读取网关传递的 X-User-Id / X-User-Role，兜底从 Authorization 头解析 JWT。
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        if (userIdHeader != null) {
            UserContextHolder.set(new UserContext(Long.valueOf(userIdHeader), roleHeader));
        } else {
            // 兜底：从 Authorization 头解析 JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (JwtUtil.isTokenValid(token)) {
                    UserContextHolder.set(new UserContext(
                            JwtUtil.getUserId(token),
                            JwtUtil.getRole(token)
                    ));
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        UserContextHolder.clear();
    }
}
