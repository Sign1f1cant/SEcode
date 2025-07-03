// 增强版本的ChangeController.java - 集成审查功能
// src/main/java/com/yourcompany/simplecodereview/controller/ChangeController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.Change;
import com.yourcompany.simplecodereview.entity.ChangeStatus;
import com.yourcompany.simplecodereview.service.ChangeService;
import com.yourcompany.simplecodereview.service.ReviewService;
import com.yourcompany.simplecodereview.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/changes")
@CrossOrigin(origins = "*")
public class ChangeController {

    @Autowired
    private ChangeService changeService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CommentService commentService;

    /**
     * 创建新变更（从Git提交）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createChange(@RequestBody Map<String, String> request) {
        try {
            String projectName = request.get("projectName");
            String branch = request.get("branch");
            String commitId = request.get("commitId");
            String token = request.get("token");

            // 参数验证
            if (projectName == null || projectName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("项目名称不能为空"));
            }
            if (branch == null || branch.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("分支名称不能为空"));
            }
            if (commitId == null || commitId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("提交ID不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            // 解析用户ID
            Long submitterId = changeService.getUserIdFromToken(token.replace("Bearer ", ""));

            Change change = changeService.createChange(
                    projectName.trim(),
                    branch.trim(),
                    commitId.trim(),
                    submitterId
            );

            return ResponseEntity.ok(createSuccessResponse("变更创建成功", change));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 手动创建变更（提供完整信息）
     */
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> createManualChange(@RequestBody Map<String, String> request) {
        try {
            String projectName = request.get("projectName");
            String branch = request.get("branch");
            String commitId = request.get("commitId");
            String commitMessage = request.get("commitMessage");
            String authorName = request.get("authorName");
            String authorEmail = request.get("authorEmail");
            String token = request.get("token");

            // 参数验证
            if (projectName == null || projectName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("项目名称不能为空"));
            }
            if (commitId == null || commitId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("提交ID不能为空"));
            }
            if (commitMessage == null || commitMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("提交信息不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            // 解析用户ID
            Long submitterId = changeService.getUserIdFromToken(token.replace("Bearer ", ""));

            Change change = changeService.createChangeFromCommitInfo(
                    projectName.trim(),
                    branch != null ? branch.trim() : "main",
                    commitId.trim(),
                    commitMessage.trim(),
                    authorName != null ? authorName.trim() : "Unknown",
                    authorEmail != null ? authorEmail.trim() : "unknown@example.com",
                    submitterId
            );

            return ResponseEntity.ok(createSuccessResponse("变更创建成功", change));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取所有变更或筛选变更
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getChanges(
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorEmail,
            @RequestParam(required = false) String token) {
        try {
            List<Change> changes;

            if (projectName != null && status != null) {
                ChangeStatus changeStatus = ChangeStatus.valueOf(status.toUpperCase());
                changes = changeService.getChangesByProjectAndStatus(projectName, changeStatus);
            } else if (projectName != null) {
                changes = changeService.getChangesByProject(projectName);
            } else if (status != null) {
                ChangeStatus changeStatus = ChangeStatus.valueOf(status.toUpperCase());
                changes = changeService.getChangesByStatus(changeStatus);
            } else if (authorEmail != null) {
                changes = changeService.getChangesByAuthor(authorEmail);
            } else if (token != null) {
                // 获取当前用户提交的变更
                Long submitterId = changeService.getUserIdFromToken(token.replace("Bearer ", ""));
                changes = changeService.getChangesBySubmitter(submitterId);
            } else {
                // 默认获取所有开放状态的变更
                changes = changeService.getChangesByStatus(ChangeStatus.OPEN);
            }

            Map<String, Object> response = createSuccessResponse("获取变更列表成功", null);
            response.put("changes", changes);
            response.put("count", changes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据ID获取变更详情（包含审查和评论信息）
     */
    @GetMapping("/{changeId}")
    public ResponseEntity<Map<String, Object>> getChange(@PathVariable String changeId) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            // 获取审查信息
            Map<String, Object> reviewStats = reviewService.getReviewStats(changeId);

            // 获取评论统计
            Map<String, Object> commentStats = commentService.getCommentStats(changeId);

            Map<String, Object> response = createSuccessResponse("获取变更成功", change);
            response.put("reviewStats", reviewStats);
            response.put("commentStats", commentStats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的完整信息（变更+审查+评论）
     */
    @GetMapping("/{changeId}/full")
    public ResponseEntity<Map<String, Object>> getChangeFullInfo(@PathVariable String changeId) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            // 获取审查列表
            var reviews = reviewService.getReviewsByChange(changeId);

            // 获取评论列表
            var comments = commentService.getCommentsByChange(changeId);

            // 获取行级评论
            var inlineComments = commentService.getInlineCommentsByChange(changeId);

            // 获取统计信息
            Map<String, Object> reviewStats = reviewService.getReviewStats(changeId);
            Map<String, Object> commentStats = commentService.getCommentStats(changeId);

            Map<String, Object> response = createSuccessResponse("获取变更完整信息成功", change);
            response.put("reviews", reviews);
            response.put("comments", comments);
            response.put("inlineComments", inlineComments);
            response.put("reviewStats", reviewStats);
            response.put("commentStats", commentStats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更涉及的文件列表
     */
    @GetMapping("/{changeId}/files")
    public ResponseEntity<Map<String, Object>> getChangedFiles(@PathVariable String changeId) {
        try {
            List<String> files = changeService.getChangedFiles(changeId);

            Map<String, Object> response = createSuccessResponse("获取变更文件成功", null);
            response.put("files", files);
            response.put("count", files.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 更新变更状态
     */
    @PutMapping("/{changeId}/status")
    public ResponseEntity<Map<String, Object>> updateChangeStatus(
            @PathVariable String changeId,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("状态不能为空"));
            }

            ChangeStatus newStatus = ChangeStatus.valueOf(statusStr.toUpperCase());
            Change change = changeService.updateChangeStatus(changeId, newStatus);

            return ResponseEntity.ok(createSuccessResponse("状态更新成功", change));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("无效的状态值"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 废弃变更
     */
    @PostMapping("/{changeId}/abandon")
    public ResponseEntity<Map<String, Object>> abandonChange(
            @PathVariable String changeId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            Change change = changeService.abandonChange(changeId, reason);

            return ResponseEntity.ok(createSuccessResponse("变更已废弃", change));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 合并变更（增强版本 - 检查审查状态）
     */
    @PostMapping("/{changeId}/merge")
    public ResponseEntity<Map<String, Object>> mergeChange(
            @PathVariable String changeId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            // 检查审查状态
            Map<String, Object> reviewStats = reviewService.getReviewStats(changeId);
            Boolean canMerge = (Boolean) reviewStats.get("canMerge");

            if (!canMerge) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "变更未通过审查，无法合并。请确保有正面评价且没有负面评价。"));
            }

            // 检查是否有未解决的评论
            Map<String, Object> commentStats = commentService.getCommentStats(changeId);
            Long unresolvedComments = (Long) commentStats.get("unresolvedComments");

            String forceStr = request != null ? request.get("force") : null;
            boolean force = "true".equals(forceStr);

            if (unresolvedComments > 0 && !force) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                        "存在未解决的评论，无法合并。请解决所有评论或使用强制合并。"));
            }

            Change change = changeService.mergeChange(changeId);

            return ResponseEntity.ok(createSuccessResponse("变更合并成功", change));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取项目的变更统计（增强版本 - 包含审查统计）
     */
    @GetMapping("/stats/{projectName}")
    public ResponseEntity<Map<String, Object>> getProjectStats(@PathVariable String projectName) {
        try {
            long openChanges = changeService.getOpenChangesCount(projectName);
            List<Change> allChanges = changeService.getChangesByProject(projectName);

            long mergedChanges = allChanges.stream()
                    .filter(c -> c.getStatus() == ChangeStatus.MERGED)
                    .count();
            long abandonedChanges = allChanges.stream()
                    .filter(c -> c.getStatus() == ChangeStatus.ABANDONED)
                    .count();

            // 计算审查统计
            long totalReviews = 0;
            long pendingReviews = 0;
            long totalComments = 0;

            for (Change change : allChanges) {
                Map<String, Object> reviewStats = reviewService.getReviewStats(change.getChangeId());
                Map<String, Object> commentStats = commentService.getCommentStats(change.getChangeId());

                totalReviews += (Long) reviewStats.get("totalReviews");
                pendingReviews += (Long) reviewStats.get("pendingReviews");
                totalComments += (Long) commentStats.get("totalComments");
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("projectName", projectName);
            stats.put("totalChanges", allChanges.size());
            stats.put("openChanges", openChanges);
            stats.put("mergedChanges", mergedChanges);
            stats.put("abandonedChanges", abandonedChanges);
            stats.put("totalReviews", totalReviews);
            stats.put("pendingReviews", pendingReviews);
            stats.put("totalComments", totalComments);

            Map<String, Object> response = createSuccessResponse("获取项目统计成功", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取我的变更（包含审查信息）
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyChanges(@RequestParam String token) {
        try {
            Long submitterId = changeService.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Change> changes = changeService.getChangesBySubmitter(submitterId);

            // 为每个变更添加审查统计
            List<Map<String, Object>> enrichedChanges = changes.stream().map(change -> {
                Map<String, Object> changeData = new HashMap<>();
                changeData.put("change", change);
                changeData.put("reviewStats", reviewService.getReviewStats(change.getChangeId()));
                changeData.put("commentStats", commentService.getCommentStats(change.getChangeId()));
                return changeData;
            }).toList();

            Map<String, Object> response = createSuccessResponse("获取我的变更成功", null);
            response.put("changes", enrichedChanges);
            response.put("count", changes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取需要我审查的变更列表
     */
    @GetMapping("/pending-review")
    public ResponseEntity<Map<String, Object>> getPendingReviewChanges(@RequestParam String token) {
        try {
            Long reviewerId = changeService.getUserIdFromToken(token.replace("Bearer ", ""));
            var pendingReviews = reviewService.getPendingReviewsByReviewer(reviewerId);

            // 获取对应的变更信息
            List<Map<String, Object>> pendingChanges = pendingReviews.stream().map(review -> {
                Change change = changeService.getChangeById(review.getChangeId()).orElse(null);
                Map<String, Object> data = new HashMap<>();
                data.put("review", review);
                data.put("change", change);
                return data;
            }).toList();

            Map<String, Object> response = createSuccessResponse("获取待审查变更成功", null);
            response.put("pendingChanges", pendingChanges);
            response.put("count", pendingChanges.size());

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