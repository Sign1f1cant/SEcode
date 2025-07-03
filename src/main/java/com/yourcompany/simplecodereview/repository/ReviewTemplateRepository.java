// src/main/java/com/yourcompany/simplecodereview/repository/ReviewTemplateRepository.java
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.ReviewTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long> {

    /**
     * 根据名称查找模板
     */
    Optional<ReviewTemplate> findByName(String name);

    /**
     * 检查模板名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取所有活跃的模板
     */
    List<ReviewTemplate> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据项目名称获取模板
     */
    List<ReviewTemplate> findByProjectNameAndIsActiveTrueOrderByCreatedAtDesc(String projectName);

    /**
     * 获取全局模板（不绑定特定项目）
     */
    List<ReviewTemplate> findByProjectNameIsNullAndIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据创建者获取模板
     */
    List<ReviewTemplate> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(Long createdBy);
}