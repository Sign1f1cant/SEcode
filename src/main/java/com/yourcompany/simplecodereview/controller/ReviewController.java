// src/main/java/com/yourcompany/simplecodereview/controller/ReviewController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.Review;
import com.yourcompany.simplecodereview.entity.ReviewScore;
import com.yourcompany.simplecodereview.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 分配审查者到变更
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignReviewer(@RequestBody Map<String, String> request) {
        try {
            String changeId = request.get("changeId");
            String reviewerIdStr = request.get("reviewerId");
            String token = request.get("token");

            if (changeId == null || changeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("变更ID不能为空"));
            }
            if (reviewerIdStr == null || reviewerIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("审查者ID不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long reviewerId = Long.parseLong(reviewerIdStr);
            Long assignerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));

            Review review = reviewService.assignReviewer(changeId, reviewerId, assignerId);

            return ResponseEntity.ok(createSuccessResponse("审查者分配成功", review));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 批量分配审查者
     */
    @PostMapping("/assign-multiple")
    public ResponseEntity<Map<String, Object>> assignMultipleReviewers(@RequestBody Map<String, Object> request) {
        try {
            String changeId = (String) request.get("changeId");
            @SuppressWarnings("unchecked")
            List<String> reviewerIdStrs = (List<String>) request.get("reviewerIds");
            String token = (String) request.get("token");

            if (changeId == null || changeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("变更ID不能为空"));
            }
            if (reviewerIdStrs == null || reviewerIdStrs.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("审查者ID列表不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            List<Long> reviewerIds = reviewerIdStrs.stream()
                    .map(Long::parseLong)
                    .toList();
            Long assignerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));

            List<Review> reviews = reviewService.assignMultipleReviewers(changeId, reviewerIds, assignerId);

            Map<String, Object> response = createSuccessResponse("批量分配审查者成功", null);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 提交审查意见
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, String> request) {
        try {
            String changeId = request.get("changeId");
            String scoreStr = request.get("score");
            String message = request.get("message");
            String token = request.get("token");

            if (changeId == null || changeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("变更ID不能为空"));
            }
            if (scoreStr == null || scoreStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("评分不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            ReviewScore score = ReviewScore.valueOf(scoreStr.toUpperCase());
            Long reviewerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));

            Review review = reviewService.submitReview(changeId, reviewerId, score, message);

            return ResponseEntity.ok(createSuccessResponse("审查意见提交成功", review));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("无效的评分值"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 开始审查
     */
    @PostMapping("/{changeId}/start")
    public ResponseEntity<Map<String, Object>> startReview(
            @PathVariable String changeId,
            @RequestParam String token) {
        try {
            Long reviewerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));
            Review review = reviewService.startReview(changeId, reviewerId);

            return ResponseEntity.ok(createSuccessResponse("开始审查", review));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 拒绝审查
     */
    @PostMapping("/{changeId}/decline")
    public ResponseEntity<Map<String, Object>> declineReview(
            @PathVariable String changeId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long reviewerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));
            Review review = reviewService.declineReview(changeId, reviewerId, reason);

            return ResponseEntity.ok(createSuccessResponse("拒绝审查", review));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 移除审查者
     */
    @DeleteMapping("/{changeId}/reviewer/{reviewerId}")
    public ResponseEntity<Map<String, Object>> removeReviewer(
            @PathVariable String changeId,
            @PathVariable Long reviewerId,
            @RequestParam String token) {
        try {
            Long removerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));
            reviewService.removeReviewer(changeId, reviewerId, removerId);

            return ResponseEntity.ok(createSuccessResponse("审查者移除成功", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的所有审查
     */
    @GetMapping("/change/{changeId}")
    public ResponseEntity<Map<String, Object>> getReviewsByChange(@PathVariable String changeId) {
        try {
            List<Review> reviews = reviewService.getReviewsByChange(changeId);

            Map<String, Object> response = createSuccessResponse("获取变更审查列表成功", null);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取审查者的审查列表
     */
    @GetMapping("/reviewer")
    public ResponseEntity<Map<String, Object>> getReviewsByReviewer(@RequestParam String token) {
        try {
            Long reviewerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Review> reviews = reviewService.getReviewsByReviewer(reviewerId);

            Map<String, Object> response = createSuccessResponse("获取审查者审查列表成功", null);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取我的待处理审查
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingReviews(@RequestParam String token) {
        try {
            Long reviewerId = reviewService.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Review> reviews = reviewService.getPendingReviewsByReviewer(reviewerId);

            Map<String, Object> response = createSuccessResponse("获取待处理审查成功", null);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取所有待处理审查（管理功能）
     */
    @GetMapping("/pending/all")
    public ResponseEntity<Map<String, Object>> getAllPendingReviews() {
        try {
            List<Review> reviews = reviewService.getPendingReviews();

            Map<String, Object> response = createSuccessResponse("获取所有待处理审查成功", null);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的审查统计
     */
    @GetMapping("/stats/{changeId}")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable String changeId) {
        try {
            Map<String, Object> stats = reviewService.getReviewStats(changeId);

            Map<String, Object> response = createSuccessResponse("获取审查统计成功", stats);
            return ResponseEntity.ok(response);

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