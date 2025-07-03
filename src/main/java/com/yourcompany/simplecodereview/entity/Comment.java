// src/main/java/com/yourcompany/simplecodereview/entity/Comment.java
package com.yourcompany.simplecodereview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_id", nullable = false)
    private String changeId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(length = 4000, nullable = false)
    private String content;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "line_type")
    private String lineType; // "OLD", "NEW", "CONTEXT"

    @Enumerated(EnumType.STRING)
    private ReviewCommentType type = ReviewCommentType.GENERAL;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    // 构造函数
    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Comment(String changeId, Long authorId, String content) {
        this();
        this.changeId = changeId;
        this.authorId = authorId;
        this.content = content;
    }

    // 行级评论构造函数
    public Comment(String changeId, Long authorId, String content,
                   String filePath, Integer lineNumber, String lineType) {
        this(changeId, authorId, content);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.lineType = lineType;
        this.type = ReviewCommentType.INLINE;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getLineType() { return lineType; }
    public void setLineType(String lineType) { this.lineType = lineType; }

    public ReviewCommentType getType() { return type; }
    public void setType(ReviewCommentType type) { this.type = type; }

    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}