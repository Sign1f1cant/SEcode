// src/main/java/com/yourcompany/simplecodereview/repository/CommentRepository.java
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.Comment;
import com.yourcompany.simplecodereview.entity.ReviewCommentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据变更ID获取所有评论（按创建时间排序）
     */
    List<Comment> findByChangeIdOrderByCreatedAtAsc(String changeId);

    /**
     * 根据审查ID获取评论
     */
    List<Comment> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 根据作者获取评论
     */
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    /**
     * 获取变更中的行级评论
     */
    List<Comment> findByChangeIdAndTypeOrderByFilePathAscLineNumberAsc(String changeId, ReviewCommentType type);

    /**
     * 获取特定文件的行级评论
     */
    List<Comment> findByChangeIdAndFilePathAndTypeOrderByLineNumberAsc(String changeId, String filePath, ReviewCommentType type);

    /**
     * 获取特定行的评论
     */
    List<Comment> findByChangeIdAndFilePathAndLineNumberAndTypeOrderByCreatedAtAsc(
            String changeId, String filePath, Integer lineNumber, ReviewCommentType type);

    /**
     * 获取父评论的回复
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    /**
     * 获取未解决的评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.changeId = :changeId AND c.isResolved = false")
    long countUnresolvedCommentsByChange(@Param("changeId") String changeId);

    /**
     * 获取变更中的评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.changeId = :changeId")
    long countCommentsByChange(@Param("changeId") String changeId);

    /**
     * 获取变更中特定类型的评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.changeId = :changeId AND c.type = :type")
    long countCommentsByChangeAndType(@Param("changeId") String changeId, @Param("type") ReviewCommentType type);

    /**
     * 检查特定行是否有未解决的评论
     */
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.changeId = :changeId AND c.filePath = :filePath AND c.lineNumber = :lineNumber AND c.isResolved = false")
    boolean hasUnresolvedCommentsOnLine(@Param("changeId") String changeId, @Param("filePath") String filePath, @Param("lineNumber") Integer lineNumber);
}