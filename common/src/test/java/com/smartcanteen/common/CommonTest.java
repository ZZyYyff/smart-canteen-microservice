package com.smartcanteen.common;

import com.smartcanteen.common.context.UserContext;
import com.smartcanteen.common.context.UserContextHolder;
import com.smartcanteen.common.enums.*;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.common.jwt.JwtUtil;
import com.smartcanteen.common.result.ErrorResponse;
import com.smartcanteen.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * common 模块单元测试
 */
class CommonTest {

    // ==================== Result 测试 ====================

    @Test
    @DisplayName("Result.success() 不携带数据")
    void testResultSuccessNoData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.success(T data) 携带数据")
    void testResultSuccessWithData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("hello", result.getData());
    }

    @Test
    @DisplayName("Result.fail(String message) 使用默认业务异常码")
    void testResultFailWithMessage() {
        Result<Void> result = Result.fail("库存不足");
        assertEquals(1001, result.getCode());
        assertEquals("库存不足", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Result.fail(Integer code, String message) 自定义错误码")
    void testResultFailWithCode() {
        Result<Void> result = Result.fail(3002, "库存不足");
        assertEquals(3002, result.getCode());
        assertEquals("库存不足", result.getMessage());
    }

    @Test
    @DisplayName("Result.error 兼容方法")
    void testResultErrorCompatibility() {
        Result<Void> r1 = Result.error("服务器错误");
        assertEquals(500, r1.getCode());

        Result<Void> r2 = Result.error(400, "参数错误");
        assertEquals(400, r2.getCode());
    }

    // ==================== BusinessException 测试 ====================

    @Test
    @DisplayName("BusinessException 使用 ErrorCode 枚举")
    void testBusinessExceptionWithErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.PARAM_ERROR);
        assertEquals(400, ex.getCode());
        assertEquals("请求参数错误", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException 使用 ErrorCode + 自定义消息")
    void testBusinessExceptionWithCustomMessage() {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND, "用户 ID=999 不存在");
        assertEquals(404, ex.getCode());
        assertEquals("用户 ID=999 不存在", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException 自定义错误码和消息")
    void testBusinessExceptionCustom() {
        BusinessException ex = new BusinessException(2001, "用户名已存在");
        assertEquals(2001, ex.getCode());
        assertEquals("用户名已存在", ex.getMessage());
    }

    // ==================== ErrorCode 测试 ====================

    @Test
    @DisplayName("ErrorCode 枚举值数量正确")
    void testErrorCodeCount() {
        assertEquals(8, ErrorCode.values().length);
    }

    @Test
    @DisplayName("ErrorCode SUCCESS 码为 200")
    void testErrorCodeSuccess() {
        assertEquals(200, ErrorCode.SUCCESS.getCode());
    }

    // ==================== JwtUtil 测试 ====================

    @Test
    @DisplayName("生成 Token 并解析出 userId 和 role")
    void testJwtGenerateAndParse() {
        String token = JwtUtil.generateToken(100L, "STUDENT");

        assertNotNull(token);
        assertEquals(100L, JwtUtil.getUserId(token));
        assertEquals("STUDENT", JwtUtil.getRole(token));
    }

    @Test
    @DisplayName("Token 有效期内 isTokenValid 返回 true")
    void testJwtIsTokenValid() {
        String token = JwtUtil.generateToken(1L, "MERCHANT");
        assertTrue(JwtUtil.isTokenValid(token));
        assertFalse(JwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("无效 Token 的 isTokenValid 返回 false")
    void testJwtInvalidToken() {
        assertFalse(JwtUtil.isTokenValid("invalid.token.string"));
        assertTrue(JwtUtil.isTokenExpired("invalid.token.string"));
    }

    // ==================== UserContextHolder 测试 ====================

    @Test
    @DisplayName("设置和获取用户上下文")
    void testUserContextHolderSetAndGet() {
        UserContext ctx = new UserContext(42L, "ADMIN");
        UserContextHolder.set(ctx);

        assertEquals(42L, UserContextHolder.getUserId());
        assertEquals("ADMIN", UserContextHolder.getRole());

        UserContext retrieved = UserContextHolder.get();
        assertNotNull(retrieved);
        assertEquals(42L, retrieved.getUserId());

        UserContextHolder.clear();
        assertNull(UserContextHolder.get());
        assertNull(UserContextHolder.getUserId());
    }

    // ==================== ErrorResponse 测试 ====================

    @Test
    @DisplayName("ErrorResponse 快速构建")
    void testErrorResponseOf() {
        ErrorResponse resp = ErrorResponse.of(400, "参数错误");
        assertEquals(400, resp.getCode());
        assertEquals("参数错误", resp.getMessage());
        assertNotNull(resp.getTimestamp());
    }

    @Test
    @DisplayName("ErrorResponse 带详细描述构建")
    void testErrorResponseWithDetail() {
        ErrorResponse resp = ErrorResponse.of(500, "系统异常", "java.lang.NullPointerException");
        assertEquals(500, resp.getCode());
        assertEquals("系统异常", resp.getMessage());
        assertEquals("java.lang.NullPointerException", resp.getDetail());
    }

    // ==================== 枚举完整性测试 ====================

    @Test
    @DisplayName("UserRole 包含三种角色")
    void testUserRoleEnum() {
        assertThat(UserRole.values()).containsExactly(UserRole.STUDENT, UserRole.MERCHANT, UserRole.ADMIN);
        assertEquals("学生", UserRole.STUDENT.getDescription());
        assertEquals("商家", UserRole.MERCHANT.getDescription());
        assertEquals("管理员", UserRole.ADMIN.getDescription());
    }

    @Test
    @DisplayName("UserStatus 枚举值正确")
    void testUserStatusEnum() {
        assertEquals("正常", UserStatus.NORMAL.getDescription());
        assertEquals("已禁用", UserStatus.DISABLED.getDescription());
    }

    @Test
    @DisplayName("DishStatus 枚举值正确")
    void testDishStatusEnum() {
        assertEquals("上架中", DishStatus.ON_SALE.getDescription());
        assertEquals("已下架", DishStatus.OFF_SALE.getDescription());
    }

    @Test
    @DisplayName("OrderStatus 包含全部 6 种状态")
    void testOrderStatusEnum() {
        assertEquals(6, OrderStatus.values().length);
        assertThat(OrderStatus.values()).contains(
                OrderStatus.CREATED,
                OrderStatus.ACCEPTED,
                OrderStatus.COOKING,
                OrderStatus.WAIT_PICKUP,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("PickupQueueStatus 包含全部 4 种状态")
    void testPickupQueueStatusEnum() {
        assertEquals(4, PickupQueueStatus.values().length);
        assertThat(PickupQueueStatus.values()).contains(
                PickupQueueStatus.WAITING,
                PickupQueueStatus.CALLED,
                PickupQueueStatus.FINISHED,
                PickupQueueStatus.CANCELLED
        );
    }
}
