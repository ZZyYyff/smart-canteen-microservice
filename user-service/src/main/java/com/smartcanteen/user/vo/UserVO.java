package com.smartcanteen.user.vo;

import com.smartcanteen.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO，返回前端时隐藏密码等敏感字段
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String phone;
    private String studentNo;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    /** 从实体构建 VO，过滤敏感字段 */
    public static UserVO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .studentNo(user.getStudentNo())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
