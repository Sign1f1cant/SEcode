package com.yourcompany.simplecodereview.entity;

/**
 * 仓库类型
 */
public enum RepositoryType {
    GIT_EXTERNAL("外部Git仓库"),
    GIT_LOCAL("本地Git仓库"),
    FILE_UPLOAD("文件上传"),
    MANUAL_INPUT("手动输入");

    private final String description;
    RepositoryType(String description) { this.description = description; }
    public String getDescription() { return description; }
}