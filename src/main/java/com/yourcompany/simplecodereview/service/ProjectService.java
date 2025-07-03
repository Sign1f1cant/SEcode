// 在 ProjectService.java 中添加用户相关方法
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.Project;
import com.yourcompany.simplecodereview.entity.User;
import com.yourcompany.simplecodereview.repository.ProjectRepository;
import com.yourcompany.simplecodereview.repository.UserRepository;
import com.yourcompany.simplecodereview.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GitUtil gitUtil;

    /**
     * 创建新项目（需要用户ID）
     */
    public Project createProject(String name, String description, String gitPath, Long creatorId) {
        // 验证用户是否存在
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + creatorId));

        // 检查项目是否已存在
        if (projectRepository.existsById(name)) {
            throw new RuntimeException("项目已存在: " + name);
        }

        // 验证Git仓库路径
        if (!isValidGitRepository(gitPath)) {
            throw new RuntimeException("无效的Git仓库路径: " + gitPath);
        }

        Project project = new Project(name, description, gitPath, creatorId);

        // 尝试获取默认分支
        try {
            String defaultBranch = gitUtil.getDefaultBranch(gitPath);
            project.setDefaultBranch(defaultBranch);
        } catch (Exception e) {
            project.setDefaultBranch("main");
        }

        return projectRepository.save(project);
    }

    /**
     * 从token解析用户ID
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("user_")) {
            throw new RuntimeException("无效的token");
        }

        try {
            return Long.parseLong(token.substring(5));
        } catch (NumberFormatException e) {
            throw new RuntimeException("token格式错误");
        }
    }

    /**
     * 验证用户是否有权限操作项目
     */
    public boolean hasProjectPermission(String projectName, Long userId) {
        Project project = projectRepository.findById(projectName).orElse(null);
        if (project == null) {
            return false;
        }

        // 项目创建者有权限
        if (project.getCreatorId().equals(userId)) {
            return true;
        }

        // 管理员有权限（可以后续扩展）
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && "ADMIN".equals(user.getRole().name())) {
            return true;
        }

        return false;
    }

    /**
     * 获取用户创建的项目
     */
    public List<Project> getProjectsByCreator(Long creatorId) {
        return projectRepository.findByCreatorIdAndIsActiveTrueOrderByCreatedAtDesc(creatorId);
    }

    // 保留原有的其他方法...
    public List<Project> getAllActiveProjects() {
        return projectRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public Optional<Project> getProjectByName(String name) {
        return projectRepository.findById(name);
    }

    public Project updateProject(String name, String description, String gitPath, Long userId) {
        Project project = projectRepository.findById(name)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + name));

        // 检查权限
        if (!hasProjectPermission(name, userId)) {
            throw new RuntimeException("没有权限修改此项目");
        }

        if (description != null) {
            project.setDescription(description);
        }

        if (gitPath != null && !gitPath.equals(project.getGitPath())) {
            if (!isValidGitRepository(gitPath)) {
                throw new RuntimeException("无效的Git仓库路径: " + gitPath);
            }
            project.setGitPath(gitPath);
        }

        return projectRepository.save(project);
    }

    public void deleteProject(String name, Long userId) {
        Project project = projectRepository.findById(name)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + name));

        // 检查权限
        if (!hasProjectPermission(name, userId)) {
            throw new RuntimeException("没有权限删除此项目");
        }

        project.setIsActive(false);
        projectRepository.save(project);
    }

    public List<String> getProjectBranches(String projectName) {
        Project project = getProjectByName(projectName)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

        try {
            return gitUtil.getBranches(project.getGitPath());
        } catch (Exception e) {
            throw new RuntimeException("获取分支失败: " + e.getMessage());
        }
    }

    private boolean isValidGitRepository(String gitPath) {
        try {
            // 检查是否为远程URL
            if (gitPath.startsWith("http://") || gitPath.startsWith("https://") || gitPath.startsWith("git@")) {
                return gitPath.endsWith(".git") || gitPath.contains("github.com") || gitPath.contains("gitlab.com");
            }

            // 检查本地路径
            File gitDir = new File(gitPath);
            File gitFolder = new File(gitDir, ".git");
            return gitDir.exists() && (gitFolder.exists() || gitDir.getName().endsWith(".git"));
        } catch (Exception e) {
            return false;
        }
    }
}