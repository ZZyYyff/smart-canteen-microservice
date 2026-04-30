package com.smartcanteen.common.context;

/**
 * 用户上下文持有者，基于 ThreadLocal 实现同一请求线程内共享用户信息。
 * 典型使用方式：
 * <pre>
 *   // 在拦截器/过滤器中设置
 *   UserContextHolder.set(new UserContext(userId, role));
 *   // 在 Service 层任意位置获取
 *   Long userId = UserContextHolder.getUserId();
 *   // 请求结束后清理
 *   UserContextHolder.clear();
 * </pre>
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /** 设置当前线程的用户上下文 */
    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    /** 获取当前线程的用户上下文，可能为 null */
    public static UserContext get() {
        return CONTEXT.get();
    }

    /** 获取当前用户 ID，未设置时返回 null */
    public static Long getUserId() {
        UserContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    /** 获取当前用户角色，未设置时返回 null */
    public static String getRole() {
        UserContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getRole() : null;
    }

    /** 清除当前线程的用户上下文，防止内存泄漏 */
    public static void clear() {
        CONTEXT.remove();
    }
}
