// src/main/java/com/yourcompany/simplecodereview/service/ReviewService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.*;
import com.yourcompany.simplecodereview.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 分配审查者到变更
     */
    public Review assignReviewer(String changeId, Long reviewerId, Long assignerId) {
        // 验证变更是否存在
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

        // 验证审查者是否存在
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("审查者不存在: " + reviewerId));

        // 验证分配者是否存在
        User assigner = userRepository.findById(assignerId)
                .orElseThrow(() -> new RuntimeException("分配者不存在: " + assignerId));

        // 检查是否已经分配过
        if (reviewRepository.existsByChangeIdAndReviewerIdAndIsActiveTrue(changeId, reviewerId)) {
            throw new RuntimeException("该审查者已被分配到此变更");
        }

        // 检查是否是自己审查自己的代码
        if (change.getSubmitterId().equals(reviewerId)) {
            throw new RuntimeException("不能将变更分配给提交者本人审查");
        }

        Review review = new Review(changeId, reviewerId);
        return reviewRepository.save(review);
    }

    /**
     * 批量分配审查者
     */
    public List<Review> assignMultipleReviewers(String changeId, List<Long> reviewerIds, Long assignerId) {
        return reviewerIds.stream()
                .map(reviewerId -> assignReviewer(changeId, reviewerId, assignerId))
                .toList();
    }

    /**
     * 提交审查意见
     */
    public Review submitReview(String changeId, Long reviewerId, ReviewScore score, String message) {
        Review review = reviewRepository.findByChangeIdAndReviewerIdAndIsActiveTrue(changeId, reviewerId)
                .orElseThrow(() -> new RuntimeException("未找到审查记录"));

        review.setScore(score);
        review.setMessage(message);
        review.setStatus(ReviewStatus.COMPLETED);

        Review savedReview = reviewRepository.save(review);

        // 检查是否需要更新变更状态
        updateChangeStatusBasedOnReviews(changeId);

        return savedReview;
    }

    /**
     * 开始审查
     */
    public Review startReview(String changeId, Long reviewerId) {
        Review review = reviewRepository.findByChangeIdAndReviewerIdAndIsActiveTrue(changeId, reviewerId)
                .orElseThrow(() -> new RuntimeException("未找到审查记录"));

        review.setStatus(ReviewStatus.IN_PROGRESS);
        return reviewRepository.save(review);
    }

    /**
     * 拒绝审查
     */
    public Review declineReview(String changeId, Long reviewerId, String reason) {
        Review review = reviewRepository.findByChangeIdAndReviewerIdAndIsActiveTrue(changeId, reviewerId)
                .orElseThrow(() -> new RuntimeException("未找到审查记录"));

        review.setStatus(ReviewStatus.DECLINED);
        review.setMessage(reason);
        return reviewRepository.save(review);
    }

    /**
     * 移除审查者
     */
    public void removeReviewer(String changeId, Long reviewerId, Long removerId) {
        Review review = reviewRepository.findByChangeIdAndReviewerIdAndIsActiveTrue(changeId, reviewerId)
                .orElseThrow(() -> new RuntimeException("未找到审查记录"));

        // 只有待处理或进行中的审查可以移除
        if (review.getStatus() == ReviewStatus.COMPLETED) {
            throw new RuntimeException("已完成的审查不能移除");
        }

        review.setIsActive(false);
        reviewRepository.save(review);
    }

    /**
     * 获取变更的所有审查
     */
    public List<Review> getReviewsByChange(String changeId) {
        return reviewRepository.findByChangeIdAndIsActiveTrueOrderByCreatedAtDesc(changeId);
    }

    /**
     * 获取审查者的审查列表
     */
    public List<Review> getReviewsByReviewer(Long reviewerId) {
        return reviewRepository.findByReviewerIdAndIsActiveTrueOrderByCreatedAtDesc(reviewerId);
    }

    /**
     * 获取待处理的审查
     */
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatusAndIsActiveTrueOrderByCreatedAtDesc(ReviewStatus.PENDING);
    }

    /**
     * 获取审查者的待处理审查
     */
    public List<Review> getPendingReviewsByReviewer(Long reviewerId) {
        return reviewRepository.findByReviewerIdAndIsActiveTrueOrderByCreatedAtDesc(reviewerId)
                .stream()
                .filter(review -> review.getStatus() == ReviewStatus.PENDING)
                .toList();
    }

    /**
     * 获取变更的审查统计
     */
    public Map<String, Object> getReviewStats(String changeId) {
        Map<String, Object> stats = new HashMap<>();

        long totalReviews = reviewRepository.countActiveReviewsByChange(changeId);
        long completedReviews = reviewRepository.countCompletedReviewsByChange(changeId);
        long positiveReviews = reviewRepository.countPositiveReviewsByChange(changeId);
        long negativeReviews = reviewRepository.countNegativeReviewsByChange(changeId);

        stats.put("totalReviews", totalReviews);
        stats.put("completedReviews", completedReviews);
        stats.put("pendingReviews", totalReviews - completedReviews);
        stats.put("positiveReviews", positiveReviews);
        stats.put("negativeReviews", negativeReviews);
        stats.put("neutralReviews", completedReviews - positiveReviews - negativeReviews);

        // 计算是否可以合并
        boolean canMerge = (totalReviews > 0) && (negativeReviews == 0) && (positiveReviews > 0);
        stats.put("canMerge", canMerge);

        return stats;
    }

    /**
     * 根据审查结果更新变更状态
     */
    private void updateChangeStatusBasedOnReviews(String changeId) {
        Map<String, Object> stats = getReviewStats(changeId);

        long totalReviews = (Long) stats.get("totalReviews");
        long negativeReviews = (Long) stats.get("negativeReviews");
        long positiveReviews = (Long) stats.get("positiveReviews");

        // 如果有强烈反对的审查，自动废弃变更
        long strongNegative = reviewRepository.findByChangeIdAndIsActiveTrueOrderByCreatedAtDesc(changeId)
                .stream()
                .filter(review -> review.getScore() == ReviewScore.MINUS_TWO)
                .count();

        if (strongNegative > 0) {
            Change change = changeRepository.findById(changeId).orElse(null);
            if (change != null && change.getStatus() == ChangeStatus.OPEN) {
                change.setStatus(ChangeStatus.ABANDONED);
                changeRepository.save(change);
            }
        }
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
}
