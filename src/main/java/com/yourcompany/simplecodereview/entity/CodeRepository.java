package com.yourcompany.simplecodereview.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_repositories")
public class CodeRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String repositoryName;          // 仓库名称

    private String description;             // 仓库描述

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;                   // 仓库所有者

    @Enumerated(EnumType.STRING)
    private RepositoryType type;            // 仓库类型：GIT, UPLOAD, MANUAL

    private String externalGitUrl;          // 外部Git URL（如果是Git类型）
    private String localStoragePath;        // 本地存储路径

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数、getter、setter...
}