package com.smartcanteen.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.common.jwt.JwtUtil;
import com.smartcanteen.user.dto.LoginDTO;
import com.smartcanteen.user.dto.RegisterDTO;
import com.smartcanteen.user.dto.UpdateUserDTO;
import com.smartcanteen.user.entity.User;
import com.smartcanteen.user.mapper.UserMapper;
import com.smartcanteen.user.service.impl.UserServiceImpl;
import com.smartcanteen.user.vo.LoginVO;
import com.smartcanteen.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setPhone("13900001111");
        registerDTO.setStudentNo("2024001");
        registerDTO.setPassword("abc123");
        registerDTO.setNickname("测试用户");

        loginDTO = new LoginDTO();
        loginDTO.setPhone("13900001111");
        loginDTO.setPassword("abc123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setPhone("13900001111");
        mockUser.setStudentNo("2024001");
        mockUser.setPassword("$2a$encoded$password");
        mockUser.setNickname("测试用户");
        mockUser.setRole("STUDENT");
        mockUser.setStatus("NORMAL");
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("1. 注册成功")
    void testRegisterSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("abc123")).thenReturn("$2a$encoded$password");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        UserVO result = userService.register(registerDTO);
        assertNotNull(result);
        assertEquals("13900001111", result.getPhone());
        assertEquals("2024001", result.getStudentNo());
        assertEquals("STUDENT", result.getRole());
        assertEquals("NORMAL", result.getStatus());

        // 验证 insert 被调用且密码已加密
        verify(userMapper).insert(any(User.class));
        verify(passwordEncoder).encode("abc123");
    }

    @Test
    @DisplayName("2. 重复手机号注册失败")
    void testRegisterDuplicatePhone() {
        User existing = new User();
        existing.setPhone("13900001111");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register(registerDTO));
        assertEquals("手机号已被注册", ex.getMessage());
        // 重复时不应执行 insert
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("3. 重复学工号注册失败")
    void testRegisterDuplicateStudentNo() {
        User existing = new User();
        existing.setStudentNo("2024001");

        // 第一次 selectOne（手机号）返回 null，第二次（学工号）返回已存在用户
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register(registerDTO));
        assertEquals("学工号已被注册", ex.getMessage());
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("4. 手机号登录成功")
    void testLoginSuccessByPhone() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("abc123", "$2a$encoded$password")).thenReturn(true);

        LoginVO result = userService.login(loginDTO);
        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertNotNull(result.getUser());
        assertEquals("测试用户", result.getUser().getNickname());

        // 验证 Token 有效
        assertTrue(JwtUtil.isTokenValid(result.getToken()));
        assertEquals(1L, JwtUtil.getUserId(result.getToken()));
        assertEquals("STUDENT", JwtUtil.getRole(result.getToken()));

        // 验证 RefreshToken 有效
        assertTrue(JwtUtil.isRefreshTokenValid(result.getRefreshToken()));
    }

    @Test
    @DisplayName("5. 学工号登录成功")
    void testLoginSuccessByStudentNo() {
        loginDTO.setPhone(null);
        loginDTO.setStudentNo("2024001");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("abc123", "$2a$encoded$password")).thenReturn(true);

        LoginVO result = userService.login(loginDTO);
        assertNotNull(result.getToken());
        assertEquals("2024001", result.getUser().getStudentNo());
    }

    @Test
    @DisplayName("6. 密码错误登录失败")
    void testLoginWrongPassword() {
        loginDTO.setPassword("wrong-password");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong-password", "$2a$encoded$password")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals("密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("7. 用户不存在登录失败")
    void testLoginUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertEquals("用户不存在，请先注册", ex.getMessage());
    }

    @Test
    @DisplayName("8. 账号被禁用登录失败")
    void testLoginDisabledAccount() {
        mockUser.setStatus("DISABLED");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertThat(ex.getMessage()).contains("禁用");
    }

    // ==================== 查询当前用户测试 ====================

    @Test
    @DisplayName("9. 查询当前用户成功")
    void testGetCurrentUser() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO result = userService.getCurrentUser(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("13900001111", result.getPhone());
        assertEquals("测试用户", result.getNickname());
        // 验证 VO 正确映射了实体字段（不含密码）
        assertEquals("STUDENT", result.getRole());
        assertEquals("NORMAL", result.getStatus());
    }

    @Test
    @DisplayName("10. 查询不存在的用户")
    void testGetCurrentUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.getCurrentUser(999L));
        assertEquals("用户不存在", ex.getMessage());
    }

    // ==================== 修改用户信息测试 ====================

    @Test
    @DisplayName("11. 修改昵称成功")
    void testUpdateNickname() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setNickname("新昵称");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UserVO result = userService.updateCurrentUser(1L, dto);
        assertNotNull(result);
        verify(userMapper).updateById(any(User.class));
    }

    // ==================== Token 刷新测试 ====================

    @Test
    @DisplayName("12. 刷新 Token 成功")
    void testRefreshToken() {
        String refreshToken = JwtUtil.generateRefreshToken(1L, "STUDENT");
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        LoginVO result = userService.refreshToken(refreshToken);
        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(JwtUtil.isTokenValid(result.getToken()));
        assertTrue(JwtUtil.isRefreshTokenValid(result.getRefreshToken()));
    }

    @Test
    @DisplayName("13. 无效 RefreshToken 刷新失败")
    void testRefreshTokenInvalid() {
        assertThrows(BusinessException.class,
                () -> userService.refreshToken("invalid.token"));
    }

    @Test
    @DisplayName("14. 无输入方式的登录失败")
    void testLoginNoIdentifier() {
        loginDTO.setPhone(null);
        loginDTO.setStudentNo(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(loginDTO));
        assertThat(ex.getMessage()).contains("手机号或学工号");
    }
}
