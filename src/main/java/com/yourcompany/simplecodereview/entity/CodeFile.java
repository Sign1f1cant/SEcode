package com.yourcompany.simplecodereview.entity;
import com.yourcompany.simplecodereview.entity.ContentStorageType;
import com.yourcompany.simplecodereview.entity.FileStatus;
import com.yourcompany.simplecodereview.entity.FileType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_files")
public class CodeFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", unique = true, nullable = false)
    private String fileId;                  // 文件唯一ID

    @Column(name = "version_id", nullable = false)
    private String versionId;               // 所属版本

    @Column(name = "file_path", nullable = false)
    private String filePath;                // 文件路径（相对于仓库根目录）

    private String fileName;                // 文件名
    private String fileExtension;           // 文件扩展名
    private String mimeType;                // MIME类型

    @Enumerated(EnumType.STRING)
    private FileType fileType;              // 文件类型：TEXT, BINARY, IMAGE

    @Enumerated(EnumType.STRING)
    private FileStatus status;              // 文件状态：ADDED, MODIFIED, DELETED, RENAMED

    // 内容存储方式
    @Enumerated(EnumType.STRING)
    private ContentStorageType storageType; // INLINE, FILE_SYSTEM, OBJECT_STORAGE

    @Lob
    private String inlineContent;           // 内联内容（小文件直接存DB）

    private String externalStoragePath;     // 外部存储路径
    private String contentHash;             // 内容哈希值（用于去重和完整性校验）

    // 文件元数据
    private Integer lineCount = 0;          // 行数
    private Long fileSize = 0L;             // 文件大小
    private String encoding = "UTF-8";      // 文件编码

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数、getter、setter...
}