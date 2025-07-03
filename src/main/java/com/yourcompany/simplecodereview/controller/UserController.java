// src/main/java/com/yourcompany/simplecodereview/controller/UserController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.User;
import com.yourcompany.simplecodereview.entity.UserRole;
import com.yourcompany.simplecodereview.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String displayName = request.get("displayName");

            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名不能为空"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("邮箱不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
            }

            User user = userService.registerUser(
                    username.trim(),
                    email.trim(),
                    password,
                    displayName != null ? displayName.trim() : username.trim()
            );

            // 返回用户信息（不包含密码）
            Map<String, Object> userInfo = createUserInfo(user);
            return ResponseEntity.ok(createSuccessResponse("注册成功", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String usernameOrEmail = request.get("usernameOrEmail");
            String password = request.get("password");

            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名/邮箱不能为空"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("密码不能为空"));
            }

            User user = userService.login(usernameOrEmail.trim(), password);

            // 返回用户信息和简单的"token"（实际就是用户ID）
            Map<String, Object> userInfo = createUserInfo(user);
            userInfo.put("token", "user_" + user.getId()); // 简单的token

            return ResponseEntity.ok(createSuccessResponse("登录成功", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息（根据token）
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestParam String token) {
        try {
            // 从简单token中解析用户ID
            if (!token.startsWith("user_")) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的token"));
            }

            Long userId = Long.parseLong(token.substring(5));
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Map<String, Object> userInfo = createUserInfo(user);
            return ResponseEntity.ok(createSuccessResponse("获取用户信息成功", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取所有用户（管理功能）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();

            Map<String, Object> response = createSuccessResponse("获取用户列表成功", null);
            response.put("users", users.stream().map(this::createUserInfo).toList());
            response.put("count", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String displayName = request.get("displayName");
            String email = request.get("email");

            User user = userService.updateUser(userId, displayName, email);

            Map<String, Object> userInfo = createUserInfo(user);
            return ResponseEntity.ok(createSuccessResponse("用户信息更新成功", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("原密码和新密码不能为空"));
            }

            userService.changePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(createSuccessResponse("密码修改成功", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 辅助方法
    private Map<String, Object> createUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("role", user.getRole().name());
        userInfo.put("roleDescription", user.getRole().getDescription());
        userInfo.put("isActive", user.getIsActive());
        userInfo.put("createdAt", user.getCreatedAt().toString());
        userInfo.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : null);
        // 注意：不返回密码
        return userInfo;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}