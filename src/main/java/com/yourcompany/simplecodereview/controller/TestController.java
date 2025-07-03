// src/main/java/com/yourcompany/simplecodereview/controller/TestController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.Project;
import com.yourcompany.simplecodereview.repository.ProjectRepository;
import com.yourcompany.simplecodereview.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*") // 允许前端跨域访问（开发阶段方便）
public class TestController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GitUtil gitUtil;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, Simple Code Review System!");
        response.put("status", "success");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }

    @PostMapping("/project")
    public Map<String, Object> createProject(@RequestBody Project project) {
        try {
            Project savedProject = projectRepository.save(project);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("project", savedProject);
            response.put("message", "create project success");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @GetMapping("/projects")
    public Map<String, Object> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", projects.size());
        response.put("projects", projects);
        return response;
    }

    @GetMapping("/git-info")
    public Map<String, Object> getGitInfo(@RequestParam String gitDir) {
        try {
            String info = gitUtil.getRepositoryInfo(gitDir);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gitInfo", info);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // 数据库状态检查
    @GetMapping("/db-status")
    public Map<String, Object> checkDatabaseStatus() {
        try {
            long projectCount = projectRepository.count();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("databaseConnected", true);
            response.put("projectCount", projectCount);
            response.put("message", "db connected success");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("databaseConnected", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    // 清理测试数据
    @DeleteMapping("/cleanup")
    public Map<String, String> cleanup() {
        projectRepository.deleteAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "all message are cleaned");
        response.put("status", "success");
        return response;
    }
}