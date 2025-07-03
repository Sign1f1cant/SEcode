// src/main/java/com/yourcompany/simplecodereview/repository/ReviewRepository.java
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.Review;
import com.yourcompany.simplecodereview.entity.ReviewStatus;
import com.yourcompany.simplecodereview.entity.ReviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 根据变更ID获取所有审查
     */
    List<Review> findByChangeIdAndIsActiveTrueOrderByCreatedAtDesc(String changeId);

    /**
     * 根据审查者获取审查列表
     */
    List<Review> findByReviewerIdAndIsActiveTrueOrderByCreatedAtDesc(Long reviewerId);

    /**
     * 根据状态获取审查列表
     */
    List<Review> findByStatusAndIsActiveTrueOrderByCreatedAtDesc(ReviewStatus status);

    /**
     * 获取特定变更的特定审查者的审查
     */
    Optional<Review> findByChangeIdAndReviewerIdAndIsActiveTrue(String changeId, Long reviewerId);

    /**
     * 检查审查者是否已被分配到变更
     */
    boolean existsByChangeIdAndReviewerIdAndIsActiveTrue(String changeId, Long reviewerId);

    /**
     * 获取变更的审查统计
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.changeId = :changeId AND r.isActive = true")
    long countActiveReviewsByChange(@Param("changeId") String changeId);

    /**
     * 获取变更中已完成的审查数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.changeId = :changeId AND r.status = 'COMPLETED' AND r.isActive = true")
    long countCompletedReviewsByChange(@Param("changeId") String changeId);

    /**
     * 获取变更中有反对意见的审查数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.changeId = :changeId AND r.score IN ('MINUS_ONE', 'MINUS_TWO') AND r.isActive = true")
    long countNegativeReviewsByChange(@Param("changeId") String changeId);

    /**
     * 获取变更中支持的审查数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.changeId = :changeId AND r.score IN ('PLUS_ONE', 'PLUS_TWO') AND r.isActive = true")
    long countPositiveReviewsByChange(@Param("changeId") String changeId);

    /**
     * 获取审查者的待处理审查数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewerId = :reviewerId AND r.status = 'PENDING' AND r.isActive = true")
    long countPendingReviewsByReviewer(@Param("reviewerId") Long reviewerId);

    /**
     * 根据评分获取审查列表
     */
    List<Review> findByScoreAndIsActiveTrueOrderByCreatedAtDesc(ReviewScore score);
}