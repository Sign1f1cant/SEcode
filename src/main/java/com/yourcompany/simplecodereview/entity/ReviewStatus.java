// src/main/java/com/yourcompany/simplecodereview/entity/ReviewStatus.java
package com.yourcompany.simplecodereview.entity;

public enum ReviewStatus {
    PENDING("待审查"),
    IN_PROGRESS("审查中"),
    COMPLETED("已完成"),
    DECLINED("已拒绝");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}