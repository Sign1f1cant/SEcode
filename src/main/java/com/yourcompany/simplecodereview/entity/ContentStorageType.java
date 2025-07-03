package com.yourcompany.simplecodereview.entity;
/**
 * 内容存储类型
 */
public enum ContentStorageType {
    INLINE("内联存储"),          // 直接存储在数据库中
    FILE_SYSTEM("文件系统"),     // 存储在本地文件系统
    OBJECT_STORAGE("对象存储");  // 存储在云端对象存储（如S3）

    private final String description;
    ContentStorageType(String description) { this.description = description; }
    public String getDescription() { return description; }
}