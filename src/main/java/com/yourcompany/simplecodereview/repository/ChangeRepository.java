// src/main/java/com/yourcompany/simplecodereview/repository/ChangeRepository.java
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.Change;
import com.yourcompany.simplecodereview.entity.ChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeRepository extends JpaRepository<Change, String> {

    /**
     * 根据项目名称查找变更（按创建时间倒序）
     */
    List<Change> findByProjectNameOrderByCreatedAtDesc(String projectName);

    /**
     * 根据状态查找变更
     */
    List<Change> findByStatusOrderByCreatedAtDesc(ChangeStatus status);

    /**
     * 根据项目和状态查找变更
     */
    List<Change> findByProjectNameAndStatusOrderByCreatedAtDesc(String projectName, ChangeStatus status);

    /**
     * 根据提交者查找变更
     */
    List<Change> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId);

    /**
     * 根据作者邮箱查找变更
     */
    List<Change> findByAuthorEmailOrderByCreatedAtDesc(String authorEmail);

    /**
     * 检查commit是否已经存在
     */
    boolean existsByCommitId(String commitId);

    /**
     * 根据项目和分支查找变更
     */
    List<Change> findByProjectNameAndBranchOrderByCreatedAtDesc(String projectName, String branch);

    /**
     * 获取项目的开放变更数量
     */
    @Query("SELECT COUNT(c) FROM Change c WHERE c.projectName = :projectName AND c.status = 'OPEN'")
    long countOpenChangesByProject(@Param("projectName") String projectName);

    /**
     * 获取用户提交的开放变更数量
     */
    @Query("SELECT COUNT(c) FROM Change c WHERE c.submitterId = :submitterId AND c.status = 'OPEN'")
    long countOpenChangesBySubmitter(@Param("submitterId") Long submitterId);
}