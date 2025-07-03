// src/main/java/com/yourcompany/simplecodereview/service/UserService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.User;
import com.yourcompany.simplecodereview.entity.UserRole;
import com.yourcompany.simplecodereview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户注册
     */
    public User registerUser(String username, String email, String password, String displayName) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在: " + username);
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在: " + email);
        }

        // 创建新用户
        User user = new User(username, email, password, displayName);
        return userRepository.save(user);
    }

    /**
     * 用户登录（用户名或邮箱）
     */
    public User login(String usernameOrEmail, String password) {
        User user = null;

        // 尝试用户名登录
        if (usernameOrEmail.contains("@")) {
            // 包含@符号，当作邮箱处理
            user = userRepository.findByEmailAndPassword(usernameOrEmail, password).orElse(null);
        } else {
            // 当作用户名处理
            user = userRepository.findByUsernameAndPassword(usernameOrEmail, password).orElse(null);
        }

        if (user == null) {
            throw new RuntimeException("用户名/邮箱或密码错误");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("用户账号已被禁用");
        }

        // 更新最后登录时间
        user.updateLastLogin();
        return userRepository.save(user);
    }

    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有活跃用户
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * 更新用户信息
     */
    public User updateUser(Long userId, String displayName, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 检查邮箱是否被其他用户使用
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱已被其他用户使用: " + email);
            }
            user.setEmail(email);
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }

        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(newPassword);
        return userRepository.save(user);
    }

    /**
     * 更新用户角色（管理员功能）
     */
    public User updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * 禁用/启用用户
     */
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        user.setIsActive(!user.getIsActive());
        return userRepository.save(user);
    }

    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}