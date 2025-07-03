// src/main/java/com/yourcompany/simplecodereview/entity/ReviewTemplate.java
package com.yourcompany.simplecodereview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_templates")
public class ReviewTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "project_name")
    private String projectName;

    @Column(length = 4000)
    private String checklist; // JSON格式的检查清单

    @Column(name = "auto_assign_rules", length = 2000)
    private String autoAssignRules; // JSON格式的自动分配规则

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // 构造函数
    public ReviewTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ReviewTemplate(String name, String description, String projectName,
                          String checklist, Long createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.projectName = projectName;
        this.checklist = checklist;
        this.createdBy = createdBy;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getChecklist() { return checklist; }
    public void setChecklist(String checklist) { this.checklist = checklist; }

    public String getAutoAssignRules() { return autoAssignRules; }
    public void setAutoAssignRules(String autoAssignRules) { this.autoAssignRules = autoAssignRules; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}