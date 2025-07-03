// 在 ProjectRepository.java 中添加新的查询方法
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * 查找所有活跃的项目
     */
    List<Project> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据创建者查找项目
     */
    List<Project> findByCreatorIdAndIsActiveTrueOrderByCreatedAtDesc(Long creatorId);

    /**
     * 根据描述模糊查询
     */
    List<Project> findByDescriptionContainingIgnoreCase(String description);

    /**
     * 检查项目名是否存在
     */
    boolean existsByName(String name);
}