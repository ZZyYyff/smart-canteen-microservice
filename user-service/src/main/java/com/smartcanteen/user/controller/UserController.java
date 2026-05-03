package com.smartcanteen.user.controller;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.user.dto.LoginDTO;
import com.smartcanteen.user.dto.RegisterDTO;
import com.smartcanteen.user.dto.UpdateUserDTO;
import com.smartcanteen.user.service.UserService;
import com.smartcanteen.user.vo.LoginVO;
import com.smartcanteen.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器，只做参数接收与结果返回，业务逻辑委托给 UserService
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 用户注册 */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO vo = userService.register(dto);
        return Result.success("注册成功", vo);
    }

    /** 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success("登录成功", vo);
    }

    /** 刷新 Token */
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        LoginVO vo = userService.refreshToken(refreshToken);
        return Result.success(vo);
    }

    /** 查询当前用户信息 */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        UserVO vo = userService.getCurrentUser(userId);
        return Result.success(vo);
    }

    /** 修改当前用户信息 */
    @PutMapping("/me")
    public Result<UserVO> updateCurrentUser(@RequestHeader("X-User-Id") Long userId,
                                             @Valid @RequestBody UpdateUserDTO dto) {
        UserVO vo = userService.updateCurrentUser(userId, dto);
        return Result.success("修改成功", vo);
    }

    // ==================== 管理员接口 ====================

    /** 管理员查询用户列表 */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminUserList(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!"ADMIN".equals(role)) {
            return Result.fail(403, "无权限");
        }
        return Result.success(userService.getAdminUserList(keyword, filterRole, status, page, size));
    }

    /** 管理员启用用户 */
    @PutMapping("/admin/{id}/enable")
    public Result<Void> enableUser(@RequestHeader("X-User-Id") Long adminId,
                                    @RequestHeader("X-User-Role") String role,
                                    @PathVariable("id") Long userId) {
        if (!"ADMIN".equals(role)) {
            return Result.fail(403, "无权限");
        }
        userService.enableUser(adminId, userId);
        return Result.success();
    }

    /** 管理员禁用用户 */
    @PutMapping("/admin/{id}/disable")
    public Result<Void> disableUser(@RequestHeader("X-User-Id") Long adminId,
                                     @RequestHeader("X-User-Role") String role,
                                     @PathVariable("id") Long userId) {
        if (!"ADMIN".equals(role)) {
            return Result.fail(403, "无权限");
        }
        userService.disableUser(adminId, userId);
        return Result.success();
    }

    /** 管理员修改用户角色 */
    @PutMapping("/admin/{id}/role")
    public Result<Void> updateUserRole(@RequestHeader("X-User-Id") Long adminId,
                                        @RequestHeader("X-User-Role") String roleHeader,
                                        @PathVariable("id") Long userId,
                                        @RequestBody Map<String, String> body) {
        if (!"ADMIN".equals(roleHeader)) {
            return Result.fail(403, "无权限");
        }
        userService.updateUserRole(adminId, userId, body.get("role"));
        return Result.success();
    }
}
