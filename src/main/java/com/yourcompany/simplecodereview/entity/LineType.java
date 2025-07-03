package com.yourcompany.simplecodereview.entity;

/**
 * 行类型
 */
public enum LineType {
    CODE("代码行"),
    COMMENT("注释行"),
    BLANK("空行"),
    IMPORT("导入行"),
    ANNOTATION("注解行");

    private final String description;
    LineType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
