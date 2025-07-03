package com.yourcompany.simplecodereview.entity;
import com.yourcompany.simplecodereview.entity.LineType;
import jakarta.persistence.*;

@Entity
@Table(name = "code_lines")
public class CodeLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_id", unique = true, nullable = false)
    private String lineId;                  // 行唯一ID

    @Column(name = "file_id", nullable = false)
    private String fileId;                  // 所属文件

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;             // 行号

    @Column(length = 4000)
    private String content;                 // 行内容

    private String contentHash;             // 行内容哈希

    @Enumerated(EnumType.STRING)
    private LineType lineType;              // 行类型：CODE, COMMENT, BLANK

    // 用于差异比较
    @Column(name = "is_added")
    private Boolean isAdded = false;        // 是否是新增行

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;      // 是否是删除行

    @Column(name = "is_modified")
    private Boolean isModified = false;     // 是否是修改行

    // 构造函数、getter、setter...
}