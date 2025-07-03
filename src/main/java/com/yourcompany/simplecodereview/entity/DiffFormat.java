package com.yourcompany.simplecodereview.entity;
/**
 * 差异格式
 */
public enum DiffFormat {
    UNIFIED_DIFF("统一差异格式"),
    JSON("JSON格式"),
    CUSTOM("自定义格式");

    private final String description;
    DiffFormat(String description) { this.description = description; }
    public String getDescription() { return description; }
}