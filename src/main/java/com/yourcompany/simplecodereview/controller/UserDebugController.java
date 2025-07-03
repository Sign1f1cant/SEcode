// UserDebugController.java - 用户调试和管理控制器
// src/main/java/com/yourcompany/simplecodereview/controller/UserDebugController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.User;
import com.yourcompany.simplecodereview.entity.UserRole;
import com.yourcompany.simplecodereview.repository.UserRepository;
import com.yourcompany.simplecodereview.service.DataInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug/users")
@CrossOrigin(origins = "*")
public class UserDebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataInitService dataInitService;

    /**
     * 获取所有用户（调试用）
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            Map<String, Object> response = createSuccessResponse("获取用户列表成功", null);
            response.put("users", users);
            response.put("count", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            long totalUsers = userRepository.count();
            long adminCount = userRepository.findByRole(UserRole.ADMIN).size();
            long reviewerCount = userRepository.findByRole(UserRole.REVIEWER).size();
            long developerCount = userRepository.findByRole(UserRole.DEVELOPER).size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("adminCount", adminCount);
            stats.put("reviewerCount", reviewerCount);
            stats.put("developerCount", developerCount);
            stats.put("initializationStatus", dataInitService.isInitialized());

            return ResponseEntity.ok(createSuccessResponse("获取用户统计成功", stats));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 创建默认用户（如果不存在）
     */
    @PostMapping("/create-defaults")
    public ResponseEntity<Map<String, Object>> createDefaultUsers() {
        try {
            List<User> createdUsers = createDefaultUsersIfNotExist();

            Map<String, Object> response = createSuccessResponse("默认用户创建完成", null);
            response.put("createdUsers", createdUsers);
            response.put("count", createdUsers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 手动创建用户
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String displayName = request.get("displayName");
            String roleStr = request.get("role");

            if (username == null || email == null || password == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名、邮箱和密码不能为空"));
            }

            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户名已存在: " + username));
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(createErrorResponse("邮箱已存在: " + email));
            }

            UserRole role = UserRole.DEVELOPER; // 默认角色
            if (roleStr != null) {
                try {
                    role = UserRole.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(createErrorResponse("无效的角色: " + roleStr));
                }
            }

            User user = new User(username, email, password, displayName != null ? displayName : username);
            user.setRole(role);
            user.setIsActive(true);

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(createSuccessResponse("用户创建成功", savedUser));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在: " + userId));
            }

            return ResponseEntity.ok(createSuccessResponse("获取用户成功", user));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在: " + username));
            }

            return ResponseEntity.ok(createSuccessResponse("获取用户成功", user));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 删除用户（仅用于测试）
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户不存在: " + userId));
            }

            userRepository.delete(user);

            return ResponseEntity.ok(createSuccessResponse("用户删除成功", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 重新初始化数据
     */
    @PostMapping("/reinitialize")
    public ResponseEntity<Map<String, Object>> reinitializeData() {
        try {
            // 删除所有用户
            userRepository.deleteAll();

            // 重新创建默认用户
            List<User> createdUsers = createDefaultUsersIfNotExist();

            Map<String, Object> response = createSuccessResponse("数据重新初始化成功", null);
            response.put("createdUsers", createdUsers);
            response.put("count", createdUsers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 检查token对应的用户
     */
    @GetMapping("/check-token/{token}")
    public ResponseEntity<Map<String, Object>> checkToken(@PathVariable String token) {
        try {
            if (!token.startsWith("user_")) {
                return ResponseEntity.badRequest().body(createErrorResponse("无效的token格式"));
            }

            Long userId = Long.parseLong(token.substring(5));
            User user = userRepository.findById(userId).orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", userId);
            result.put("userExists", user != null);
            result.put("user", user);

            return ResponseEntity.ok(createSuccessResponse("Token检查完成", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 私有方法
    private List<User> createDefaultUsersIfNotExist() {
        List<User> createdUsers = new java.util.ArrayList<>();

        // 创建管理员
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@localhost", "admin123", "系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setIsActive(true);
            createdUsers.add(userRepository.save(admin));
        }

        // 创建审查者1
        if (!userRepository.existsByUsername("reviewer1")) {
            User reviewer1 = new User("reviewer1", "reviewer1@localhost", "review123", "审查者1");
            reviewer1.setRole(UserRole.REVIEWER);
            reviewer1.setIsActive(true);
            createdUsers.add(userRepository.save(reviewer1));
        }

        // 创建审查者2
        if (!userRepository.existsByUsername("reviewer2")) {
            User reviewer2 = new User("reviewer2", "reviewer2@localhost", "review123", "审查者2");
            reviewer2.setRole(UserRole.REVIEWER);
            reviewer2.setIsActive(true);
            createdUsers.add(userRepository.save(reviewer2));
        }

        // 创建开发者1
        if (!userRepository.existsByUsername("developer1")) {
            User developer1 = new User("developer1", "developer1@localhost", "dev123", "开发者1");
            developer1.setRole(UserRole.DEVELOPER);
            developer1.setIsActive(true);
            createdUsers.add(userRepository.save(developer1));
        }

        return createdUsers;
    }

    // 辅助方法
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}