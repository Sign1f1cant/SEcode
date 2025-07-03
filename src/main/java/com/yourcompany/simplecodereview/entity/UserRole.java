// src/main/java/com/yourcompany/simplecodereview/entity/UserRole.java
package com.yourcompany.simplecodereview.entity;

public enum UserRole {
    ADMIN("管理员"),
    MAINTAINER("维护者"),
    DEVELOPER("开发者"),
    REVIEWER("审查者");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}