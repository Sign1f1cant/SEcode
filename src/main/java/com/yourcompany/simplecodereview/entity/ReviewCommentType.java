// src/main/java/com/yourcompany/simplecodereview/entity/ReviewCommentType.java
package com.yourcompany.simplecodereview.entity;

public enum ReviewCommentType {
    GENERAL("普通评论"),
    INLINE("行级评论"),
    SUGGESTION("建议"),
    APPROVAL("批准"),
    ISSUE("问题");

    private final String description;

    ReviewCommentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}