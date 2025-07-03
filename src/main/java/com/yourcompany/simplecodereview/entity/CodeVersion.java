package com.yourcompany.simplecodereview.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_versions")
public class CodeVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_id", unique = true, nullable = false)
    private String versionId;               // 版本ID（类似Git的commit hash）

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;              // 所属仓库

    private String versionName;             // 版本名称（如分支名、标签名）
    private String commitMessage;           // 提交信息
    private String authorName;              // 作者名
    private String authorEmail;             // 作者邮箱

    @Column(name = "parent_version_id")
    private String parentVersionId;         // 父版本ID

    @Column(name = "created_by")
    private Long createdBy;                 // 创建者

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 统计信息
    private Integer totalFiles = 0;         // 总文件数
    private Integer totalLines = 0;         // 总行数
    private Long storageSize = 0L;          // 存储大小（字节）

    // 构造函数、getter、setter...
}