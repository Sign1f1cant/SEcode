package com.yourcompany.simplecodereview.entity;
/**
 * 文件类型
 */
public enum FileType {
    TEXT("文本文件"),
    BINARY("二进制文件"),
    IMAGE("图像文件"),
    ARCHIVE("压缩文件"),
    UNKNOWN("未知类型");

    private final String description;
    FileType(String description) { this.description = description; }
    public String getDescription() { return description; }
}