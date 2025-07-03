// src/main/java/com/yourcompany/simplecodereview/entity/ReviewScore.java
package com.yourcompany.simplecodereview.entity;

public enum ReviewScore {
    NONE("无评分", 0),
    MINUS_TWO("强烈反对", -2),
    MINUS_ONE("不推荐", -1),
    ZERO("中性", 0),
    PLUS_ONE("推荐", 1),
    PLUS_TWO("强烈推荐", 2);

    private final String description;
    private final int value;

    ReviewScore(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }
}