// 修复版本的ProjectController.java
// src/main/java/com/yourcompany/simplecodereview/controller/ProjectController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.Project;
import com.yourcompany.simplecodereview.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建新项目（需要token）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            String gitPath = request.get("gitPath");
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("需要提供用户token"));
            }

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("项目名称不能为空"));
            }

            if (gitPath == null || gitPath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Git路径不能为空"));
            }

            // 解析用户ID
            Long userId = projectService.getUserIdFromToken(token.replace("Bearer ", ""));

            Project project = projectService.createProject(
                    name.trim(),
                    description,
                    gitPath.trim(),
                    userId
            );

            return ResponseEntity.ok(createSuccessResponse("项目创建成功", project));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取所有项目
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllActiveProjects();

            Map<String, Object> response = createSuccessResponse("获取项目列表成功", null);
            response.put("projects", projects);
            response.put("count", projects.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 添加详细的错误日志
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的项目（需要token）
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyProjects(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("需要提供用户token"));
            }

            Long userId = projectService.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Project> projects = projectService.getProjectsByCreator(userId);

            Map<String, Object> response = createSuccessResponse("获取我的项目成功", null);
            response.put("projects", projects);
            response.put("count", projects.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 根据名称获取项目详情
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getProject(@PathVariable String name) {
        try {
            Project project = projectService.getProjectByName(name)
                    .orElseThrow(() -> new RuntimeException("项目不存在: " + name));
            return ResponseEntity.ok(createSuccessResponse("获取项目成功", project));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取项目的分支列表
     */
    @GetMapping("/{name}/branches")
    public ResponseEntity<Map<String, Object>> getProjectBranches(@PathVariable String name) {
        try {
            List<String> branches = projectService.getProjectBranches(name);
            Map<String, Object> response = createSuccessResponse("获取分支列表成功", null);
            response.put("branches", branches);
            response.put("count", branches.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 更新项目
     */
    @PutMapping("/{name}")
    public ResponseEntity<Map<String, Object>> updateProject(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("需要提供用户token"));
            }

            Long userId = projectService.getUserIdFromToken(token.replace("Bearer ", ""));
            String description = request.get("description");
            String gitPath = request.get("gitPath");

            Project project = projectService.updateProject(name, description, gitPath, userId);
            return ResponseEntity.ok(createSuccessResponse("项目更新成功", project));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Object>> deleteProject(
            @PathVariable String name,
            @RequestParam String token) {
        try {
            if (token == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("需要提供用户token"));
            }

            Long userId = projectService.getUserIdFromToken(token.replace("Bearer ", ""));
            projectService.deleteProject(name, userId);
            return ResponseEntity.ok(createSuccessResponse("项目删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 辅助方法
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