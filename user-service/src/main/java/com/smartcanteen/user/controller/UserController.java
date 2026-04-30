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
}
