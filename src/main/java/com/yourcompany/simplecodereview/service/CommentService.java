// src/main/java/com/yourcompany/simplecodereview/service/CommentService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.*;
import com.yourcompany.simplecodereview.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 添加普通评论
     */
    public Comment addGeneralComment(String changeId, Long authorId, String content) {
        validateChangeExists(changeId);
        validateUserExists(authorId);

        Comment comment = new Comment(changeId, authorId, content);
        comment.setType(ReviewCommentType.GENERAL);
        return commentRepository.save(comment);
    }

    /**
     * 添加行级评论
     */
    public Comment addInlineComment(String changeId, Long authorId, String content,
                                    String filePath, Integer lineNumber, String lineType) {
        validateChangeExists(changeId);
        validateUserExists(authorId);

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new RuntimeException("文件路径不能为空");
        }
        if (lineNumber == null || lineNumber < 1) {
            throw new RuntimeException("行号必须大于0");
        }
        if (lineType == null || (!lineType.equals("OLD") && !lineType.equals("NEW") && !lineType.equals("CONTEXT"))) {
            throw new RuntimeException("行类型必须是 OLD、NEW 或 CONTEXT");
        }

        Comment comment = new Comment(changeId, authorId, content, filePath, lineNumber, lineType);
        return commentRepository.save(comment);
    }

    /**
     * 回复评论
     */
    public Comment replyToComment(Long parentCommentId, Long authorId, String content) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("父评论不存在: " + parentCommentId));

        validateUserExists(authorId);

        Comment reply = new Comment(parentComment.getChangeId(), authorId, content);
        reply.setParentCommentId(parentCommentId);
        reply.setType(ReviewCommentType.GENERAL);

        // 如果父评论是行级评论，回复也设置相同的文件和行信息
        if (parentComment.getType() == ReviewCommentType.INLINE) {
            reply.setFilePath(parentComment.getFilePath());
            reply.setLineNumber(parentComment.getLineNumber());
            reply.setLineType(parentComment.getLineType());
            reply.setType(ReviewCommentType.INLINE);
        }

        return commentRepository.save(reply);
    }

    /**
     * 添加审查相关的评论
     */
    public Comment addReviewComment(Long reviewId, Long authorId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("审查不存在: " + reviewId));

        validateUserExists(authorId);

        Comment comment = new Comment(review.getChangeId(), authorId, content);
        comment.setReviewId(reviewId);
        comment.setType(ReviewCommentType.GENERAL);
        return commentRepository.save(comment);
    }

    /**
     * 更新评论内容
     */
    public Comment updateComment(Long commentId, Long authorId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        // 只有作者本人可以修改评论
        if (!comment.getAuthorId().equals(authorId)) {
            throw new RuntimeException("只有评论作者可以修改评论");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    /**
     * 解决评论
     */
    public Comment resolveComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        validateUserExists(userId);

        comment.setIsResolved(true);
        return commentRepository.save(comment);
    }

    /**
     * 重新打开评论
     */
    public Comment reopenComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        validateUserExists(userId);

        comment.setIsResolved(false);
        return commentRepository.save(comment);
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        // 只有作者本人可以删除评论
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("只有评论作者可以删除评论");
        }

        commentRepository.delete(comment);
    }

    /**
     * 获取变更的所有评论
     */
    public List<Comment> getCommentsByChange(String changeId) {
        return commentRepository.findByChangeIdOrderByCreatedAtAsc(changeId);
    }

    /**
     * 获取变更的行级评论
     */
    public List<Comment> getInlineCommentsByChange(String changeId) {
        return commentRepository.findByChangeIdAndTypeOrderByFilePathAscLineNumberAsc(changeId, ReviewCommentType.INLINE);
    }

    /**
     * 获取特定文件的行级评论
     */
    public List<Comment> getInlineCommentsByFile(String changeId, String filePath) {
        return commentRepository.findByChangeIdAndFilePathAndTypeOrderByLineNumberAsc(changeId, filePath, ReviewCommentType.INLINE);
    }

    /**
     * 获取特定行的评论
     */
    public List<Comment> getCommentsByLine(String changeId, String filePath, Integer lineNumber) {
        return commentRepository.findByChangeIdAndFilePathAndLineNumberAndTypeOrderByCreatedAtAsc(
                changeId, filePath, lineNumber, ReviewCommentType.INLINE);
    }

    /**
     * 获取评论的回复
     */
    public List<Comment> getRepliesByComment(Long parentCommentId) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId);
    }

    /**
     * 获取用户的评论
     */
    public List<Comment> getCommentsByAuthor(Long authorId) {
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    /**
     * 获取审查的评论
     */
    public List<Comment> getCommentsByReview(Long reviewId) {
        return commentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
    }

    /**
     * 获取变更的评论统计
     */
    public java.util.Map<String, Object> getCommentStats(String changeId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        long totalComments = commentRepository.countCommentsByChange(changeId);
        long unresolvedComments = commentRepository.countUnresolvedCommentsByChange(changeId);
        long inlineComments = commentRepository.countCommentsByChangeAndType(changeId, ReviewCommentType.INLINE);
        long generalComments = commentRepository.countCommentsByChangeAndType(changeId, ReviewCommentType.GENERAL);

        stats.put("totalComments", totalComments);
        stats.put("unresolvedComments", unresolvedComments);
        stats.put("resolvedComments", totalComments - unresolvedComments);
        stats.put("inlineComments", inlineComments);
        stats.put("generalComments", generalComments);

        return stats;
    }

    /**
     * 检查特定行是否有未解决的评论
     */
    public boolean hasUnresolvedCommentsOnLine(String changeId, String filePath, Integer lineNumber) {
        return commentRepository.hasUnresolvedCommentsOnLine(changeId, filePath, lineNumber);
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

    // 私有辅助方法
    private void validateChangeExists(String changeId) {
        if (!changeRepository.existsById(changeId)) {
            throw new RuntimeException("变更不存在: " + changeId);
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在: " + userId);
        }
    }
}