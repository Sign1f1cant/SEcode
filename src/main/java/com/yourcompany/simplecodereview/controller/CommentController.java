// src/main/java/com/yourcompany/simplecodereview/controller/CommentController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.entity.Comment;
import com.yourcompany.simplecodereview.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加普通评论
     */
    @PostMapping("/general")
    public ResponseEntity<Map<String, Object>> addGeneralComment(@RequestBody Map<String, String> request) {
        try {
            String changeId = request.get("changeId");
            String content = request.get("content");
            String token = request.get("token");

            if (changeId == null || changeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("变更ID不能为空"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("评论内容不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.addGeneralComment(changeId, authorId, content.trim());

            return ResponseEntity.ok(createSuccessResponse("评论添加成功", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 添加行级评论
     */
    @PostMapping("/inline")
    public ResponseEntity<Map<String, Object>> addInlineComment(@RequestBody Map<String, Object> request) {
        try {
            String changeId = (String) request.get("changeId");
            String content = (String) request.get("content");
            String filePath = (String) request.get("filePath");
            Integer lineNumber = (Integer) request.get("lineNumber");
            String lineType = (String) request.get("lineType");
            String token = (String) request.get("token");

            if (changeId == null || changeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("变更ID不能为空"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("评论内容不能为空"));
            }
            if (filePath == null || filePath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件路径不能为空"));
            }
            if (lineNumber == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("行号不能为空"));
            }
            if (lineType == null || lineType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("行类型不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.addInlineComment(
                    changeId, authorId, content.trim(), filePath.trim(), lineNumber, lineType.trim());

            return ResponseEntity.ok(createSuccessResponse("行级评论添加成功", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 回复评论
     */
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<Map<String, Object>> replyToComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String token = request.get("token");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("回复内容不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment reply = commentService.replyToComment(commentId, authorId, content.trim());

            return ResponseEntity.ok(createSuccessResponse("回复评论成功", reply));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 添加审查相关评论
     */
    @PostMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> addReviewComment(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String token = request.get("token");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("评论内容不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.addReviewComment(reviewId, authorId, content.trim());

            return ResponseEntity.ok(createSuccessResponse("审查评论添加成功", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 更新评论
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String token = request.get("token");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("评论内容不能为空"));
            }
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户token不能为空"));
            }

            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.updateComment(commentId, authorId, content.trim());

            return ResponseEntity.ok(createSuccessResponse("评论更新成功", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 解决评论
     */
    @PostMapping("/{commentId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveComment(
            @PathVariable Long commentId,
            @RequestParam String token) {
        try {
            Long userId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.resolveComment(commentId, userId);

            return ResponseEntity.ok(createSuccessResponse("评论已解决", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 重新打开评论
     */
    @PostMapping("/{commentId}/reopen")
    public ResponseEntity<Map<String, Object>> reopenComment(
            @PathVariable Long commentId,
            @RequestParam String token) {
        try {
            Long userId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            Comment comment = commentService.reopenComment(commentId, userId);

            return ResponseEntity.ok(createSuccessResponse("评论已重新打开", comment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String token) {
        try {
            Long userId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            commentService.deleteComment(commentId, userId);

            return ResponseEntity.ok(createSuccessResponse("评论删除成功", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的所有评论
     */
    @GetMapping("/change/{changeId}")
    public ResponseEntity<Map<String, Object>> getCommentsByChange(@PathVariable String changeId) {
        try {
            List<Comment> comments = commentService.getCommentsByChange(changeId);

            Map<String, Object> response = createSuccessResponse("获取变更评论成功", null);
            response.put("comments", comments);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的行级评论
     */
    @GetMapping("/change/{changeId}/inline")
    public ResponseEntity<Map<String, Object>> getInlineCommentsByChange(@PathVariable String changeId) {
        try {
            List<Comment> comments = commentService.getInlineCommentsByChange(changeId);

            Map<String, Object> response = createSuccessResponse("获取行级评论成功", null);
            response.put("comments", comments);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取特定文件的行级评论
     */
    @GetMapping("/change/{changeId}/file")
    public ResponseEntity<Map<String, Object>> getInlineCommentsByFile(
            @PathVariable String changeId,
            @RequestParam String filePath) {
        try {
            List<Comment> comments = commentService.getInlineCommentsByFile(changeId, filePath);

            Map<String, Object> response = createSuccessResponse("获取文件行级评论成功", null);
            response.put("comments", comments);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取特定行的评论
     */
    @GetMapping("/change/{changeId}/line")
    public ResponseEntity<Map<String, Object>> getCommentsByLine(
            @PathVariable String changeId,
            @RequestParam String filePath,
            @RequestParam Integer lineNumber) {
        try {
            List<Comment> comments = commentService.getCommentsByLine(changeId, filePath, lineNumber);

            Map<String, Object> response = createSuccessResponse("获取行评论成功", null);
            response.put("comments", comments);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取评论的回复
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Map<String, Object>> getRepliesByComment(@PathVariable Long commentId) {
        try {
            List<Comment> replies = commentService.getRepliesByComment(commentId);

            Map<String, Object> response = createSuccessResponse("获取评论回复成功", null);
            response.put("replies", replies);
            response.put("count", replies.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取用户的评论
     */
    @GetMapping("/author")
    public ResponseEntity<Map<String, Object>> getCommentsByAuthor(@RequestParam String token) {
        try {
            Long authorId = commentService.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Comment> comments = commentService.getCommentsByAuthor(authorId);

            Map<String, Object> response = createSuccessResponse("获取用户评论成功", null);
            response.put("comments", comments);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更的评论统计
     */
    @GetMapping("/stats/{changeId}")
    public ResponseEntity<Map<String, Object>> getCommentStats(@PathVariable String changeId) {
        try {
            Map<String, Object> stats = commentService.getCommentStats(changeId);

            Map<String, Object> response = createSuccessResponse("获取评论统计成功", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 检查特定行是否有未解决的评论
     */
    @GetMapping("/check-unresolved")
    public ResponseEntity<Map<String, Object>> checkUnresolvedCommentsOnLine(
            @RequestParam String changeId,
            @RequestParam String filePath,
            @RequestParam Integer lineNumber) {
        try {
            boolean hasUnresolved = commentService.hasUnresolvedCommentsOnLine(changeId, filePath, lineNumber);

            Map<String, Object> response = createSuccessResponse("检查完成", null);
            response.put("hasUnresolvedComments", hasUnresolved);

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