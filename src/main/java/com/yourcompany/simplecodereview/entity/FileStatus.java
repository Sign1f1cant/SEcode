package com.yourcompany.simplecodereview.entity;
/**
 * 文件状态
 */
public enum FileStatus {
    ADDED("新增"),
    MODIFIED("修改"),
    DELETED("删除"),
    RENAMED("重命名"),
    COPIED("复制"),
    UNCHANGED("未变更");

    private final String description;
    FileStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}