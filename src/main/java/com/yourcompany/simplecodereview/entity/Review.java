// src/main/java/com/yourcompany/simplecodereview/entity/Review.java
package com.yourcompany.simplecodereview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_id", nullable = false)
    private String changeId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private ReviewScore score = ReviewScore.NONE;

    @Column(length = 2000)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // 构造函数
    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Review(String changeId, Long reviewerId) {
        this();
        this.changeId = changeId;
        this.reviewerId = reviewerId;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public ReviewScore getScore() { return score; }
    public void setScore(ReviewScore score) {
        this.score = score;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
        this.updatedAt = LocalDateTime.now();
    }

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