// 修复后的 Change.java 构造函数部分
// src/main/java/com/yourcompany/simplecodereview/entity/Change.java
package com.yourcompany.simplecodereview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "changes")
public class Change {

    @Id
    private String changeId;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String branch;

    @Column(length = 1000, nullable = false)
    private String subject;

    @Column(name = "commit_id", nullable = false, length = 40)
    private String commitId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "submitter_id")
    private Long submitterId;

    @Enumerated(EnumType.STRING)
    private ChangeStatus status = ChangeStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "commit_message", length = 4000)
    private String commitMessage;

    @Column(name = "files_changed")
    private Integer filesChanged = 0;

    @Column(name = "insertions")
    private Integer insertions = 0;

    @Column(name = "deletions")
    private Integer deletions = 0;

    // 构造函数
    public Change() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 修复：添加正确的构造函数
    public Change(String changeId, String projectName, String branch, String subject,
                  String commitId, Long submitterId) {
        this();
        this.changeId = changeId;
        this.projectName = projectName;
        this.branch = branch;
        this.subject = subject;
        this.commitId = commitId;
        this.submitterId = submitterId;
    }

    // 添加另一个构造函数用于手动创建
    public Change(String changeId, String projectName, String branch, String subject,
                  String commitId, String authorName, String authorEmail, Long submitterId) {
        this(changeId, projectName, branch, subject, commitId, submitterId);
        this.authorName = authorName;
        this.authorEmail = authorEmail;
    }

    // Getter 和 Setter 方法
    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }

    public ChangeStatus getStatus() { return status; }
    public void setStatus(ChangeStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public Integer getFilesChanged() { return filesChanged; }
    public void setFilesChanged(Integer filesChanged) { this.filesChanged = filesChanged; }

    public Integer getInsertions() { return insertions; }
    public void setInsertions(Integer insertions) { this.insertions = insertions; }

    public Integer getDeletions() { return deletions; }
    public void setDeletions(Integer deletions) { this.deletions = deletions; }

    /**
     * 更新时间戳
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}