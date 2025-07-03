// src/main/java/com/yourcompany/simplecodereview/entity/ChangeStatus.java
package com.yourcompany.simplecodereview.entity;

public enum ChangeStatus {
    OPEN("开放中"),           // 等待审查
    MERGED("已合并"),         // 已合并到主分支
    ABANDONED("已废弃"),      // 已废弃
    DRAFT("草稿");           // 草稿状态

    private final String description;

    ChangeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}