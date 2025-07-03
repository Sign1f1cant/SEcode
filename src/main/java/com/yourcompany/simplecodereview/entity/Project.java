// 临时修复版本 - 简化的Project.java
// src/main/java/com/yourcompany/simplecodereview/entity/Project.java
package com.yourcompany.simplecodereview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "git_path", nullable = false, length = 500)
    private String gitPath;

    @Column(name = "default_branch")
    private String defaultBranch = "main";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // 添加创建者ID，但暂时不用JPA关联
    @Column(name = "creator_id")
    private Long creatorId;

    // 临时注释掉JPA关联，避免查询问题
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    // private User creator;

    // 构造函数
    public Project() {
        this.createdAt = LocalDateTime.now();
    }

    public Project(String name, String description, String gitPath) {
        this();
        this.name = name;
        this.description = description;
        this.gitPath = gitPath;
    }

    public Project(String name, String description, String gitPath, Long creatorId) {
        this();
        this.name = name;
        this.description = description;
        this.gitPath = gitPath;
        this.creatorId = creatorId;
    }

    // Getter 和 Setter 方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGitPath() { return gitPath; }
    public void setGitPath(String gitPath) { this.gitPath = gitPath; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    // 暂时注释掉User关联的getter/setter
    // public User getCreator() { return creator; }
    // public void setCreator(User creator) { this.creator = creator; }
}