package com.smartcanteen.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO，手机号和学工号二选一填写
 */
@Data
public class LoginDTO {

    /** 手机号（与学工号二选一） */
    private String phone;

    /** 学工号（与手机号二选一） */
    private String studentNo;

    @NotBlank(message = "密码不能为空")
    private String password;
}
