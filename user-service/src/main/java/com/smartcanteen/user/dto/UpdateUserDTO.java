package com.smartcanteen.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息请求 DTO
 */
@Data
public class UpdateUserDTO {

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
