package com.smartcanteen.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.ErrorCode;
import com.smartcanteen.common.enums.UserRole;
import com.smartcanteen.common.enums.UserStatus;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.common.jwt.JwtUtil;
import com.smartcanteen.user.dto.LoginDTO;
import com.smartcanteen.user.dto.RegisterDTO;
import com.smartcanteen.user.dto.UpdateUserDTO;
import com.smartcanteen.user.entity.User;
import com.smartcanteen.user.mapper.UserMapper;
import com.smartcanteen.user.service.UserService;
import com.smartcanteen.user.vo.LoginVO;
import com.smartcanteen.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserVO register(RegisterDTO dto) {
        // 检查手机号是否已注册
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())) != null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "手机号已被注册");
        }
        // 检查学工号是否已注册
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getStudentNo, dto.getStudentNo())) != null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "学工号已被注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setStudentNo(dto.getStudentNo());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isBlank()
                ? dto.getNickname()
                : "用户" + dto.getPhone().substring(7));
        user.setRole(UserRole.STUDENT.name());
        user.setStatus(UserStatus.NORMAL.name());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: userId={}, phone={}", user.getId(), user.getPhone());
        return UserVO.fromEntity(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 按手机号或学工号查找用户
        User user = null;
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        } else if (dto.getStudentNo() != null && !dto.getStudentNo().isBlank()) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getStudentNo, dto.getStudentNo()));
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请输入手机号或学工号");
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在，请先注册");
        }

        // 检查账号状态
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "密码错误");
        }

        // 生成双 Token
        String token = JwtUtil.generateToken(user.getId(), user.getRole());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getRole());
        log.info("用户登录成功: userId={}, phone={}", user.getId(), user.getPhone());

        return LoginVO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserVO.fromEntity(user))
                .build();
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 校验 RefreshToken 有效性
        if (!JwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "refreshToken 无效或已过期，请重新登录");
        }

        Long userId = JwtUtil.getUserId(refreshToken);
        String role = JwtUtil.getRole(refreshToken);

        // 确认用户仍然存在且未被禁用
        User user = userMapper.selectById(userId);
        if (user == null || UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已被禁用");
        }

        // 签发新的双 Token
        String newToken = JwtUtil.generateToken(userId, role);
        String newRefreshToken = JwtUtil.generateRefreshToken(userId, role);
        log.info("Token 刷新成功: userId={}", userId);

        return LoginVO.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .user(UserVO.fromEntity(user))
                .build();
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserVO updateCurrentUser(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 修改昵称
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }

        // 修改手机号（需检查唯一性）
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, dto.getPhone())
                    .ne(User::getId, userId));
            if (exist != null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "手机号已被其他用户使用");
            }
            user.setPhone(dto.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
        return UserVO.fromEntity(user);
    }

    @Override
    public Map<String, Object> getAdminUserList(String keyword, String role, String status, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getPhone, keyword)
                    .or().like(User::getStudentNo, keyword)
                    .or().like(User::getNickname, keyword));
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        long total = userMapper.selectCount(wrapper);
        List<User> users = userMapper.selectList(
                wrapper.last("LIMIT " + ((page - 1) * size) + ", " + size));
        List<UserVO> vos = users.stream().map(UserVO::fromEntity).toList();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("list", vos);
        return result;
    }

    @Override
    @Transactional
    public void enableUser(Long adminId, Long userId) {
        if (adminId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作自己");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(UserStatus.NORMAL.name());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("管理员 {} 启用用户: userId={}", adminId, userId);
    }

    @Override
    @Transactional
    public void disableUser(Long adminId, Long userId) {
        if (adminId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作自己");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(UserStatus.DISABLED.name());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("管理员 {} 禁用用户: userId={}", adminId, userId);
    }

    @Override
    @Transactional
    public void updateUserRole(Long adminId, Long userId, String role) {
        if (adminId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作自己");
        }
        try {
            UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的角色: " + role);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("管理员 {} 修改用户角色: userId={}, role={}", adminId, userId, role);
    }
}
