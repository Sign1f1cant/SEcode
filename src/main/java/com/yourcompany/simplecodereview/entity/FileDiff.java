package com.yourcompany.simplecodereview.entity;
import com.yourcompany.simplecodereview.entity.FileStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_diffs")
public class FileDiff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "diff_id", unique = true, nullable = false)
    private String diffId;                  // 差异ID

    @Column(name = "from_version_id")
    private String fromVersionId;           // 源版本

    @Column(name = "to_version_id", nullable = false)
    private String toVersionId;             // 目标版本

    @Column(name = "file_path", nullable = false)
    private String filePath;                // 文件路径

    @Enumerated(EnumType.STRING)
    private FileStatus changeType;          // 变更类型

    // 差异统计
    private Integer addedLines = 0;         // 新增行数
    private Integer deletedLines = 0;       // 删除行数
    private Integer modifiedLines = 0;      // 修改行数

    // 差异内容（可以是统一diff格式或自定义JSON格式）
    @Lob
    private String diffContent;             // 差异内容

    @Enumerated(EnumType.STRING)
    private DiffFormat diffFormat;          // 差异格式：UNIFIED_DIFF, JSON, CUSTOM

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数、getter、setter...
}