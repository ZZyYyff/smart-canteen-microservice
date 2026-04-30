package com.smartcanteen.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.common.enums.UserRole;
import com.smartcanteen.common.enums.UserStatus;
import com.smartcanteen.user.dto.LoginDTO;
import com.smartcanteen.user.dto.RegisterDTO;
import com.smartcanteen.user.service.UserService;
import com.smartcanteen.user.vo.LoginVO;
import com.smartcanteen.user.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;

    // ==================== 注册 ====================

    @Test
    @DisplayName("1. 注册成功返回 200 和用户信息")
    void testRegisterSuccess() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone("13900001111");
        dto.setStudentNo("2025001");
        dto.setPassword("123456");
        dto.setNickname("测试");

        UserVO vo = UserVO.builder()
                .id(1L)
                .phone("13900001111")
                .role(UserRole.STUDENT.name())
                .status(UserStatus.NORMAL.name())
                .build();
        when(userService.register(any())).thenReturn(vo);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phone").value("13900001111"));
    }

    @Test
    @DisplayName("2. 注册缺少手机号返回 400")
    void testRegisterMissingPhone() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setPassword("123456");
        dto.setNickname("测试");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 登录 ====================

    @Test
    @DisplayName("3. 手机号登录成功返回 Token")
    void testLoginSuccess() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setPhone("13900001111");
        dto.setPassword("123456");

        LoginVO vo = LoginVO.builder()
                .token("access-token-xxx")
                .refreshToken("refresh-token-xxx")
                .build();
        when(userService.login(any())).thenReturn(vo);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("access-token-xxx"));
    }

    @Test
    @DisplayName("4. 登录无密码返回 400")
    void testLoginMissingPassword() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setPhone("13900001111");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 查询当前用户 ====================

    @Test
    @DisplayName("5. 查询当前用户返回用户信息")
    void testGetCurrentUser() throws Exception {
        UserVO vo = UserVO.builder()
                .id(1L)
                .phone("13900001111")
                .nickname("测试用户")
                .build();
        when(userService.getCurrentUser(1L)).thenReturn(vo);

        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }
}
