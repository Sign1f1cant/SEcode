// src/main/java/com/yourcompany/simplecodereview/dto/ChangeStats.java
package com.yourcompany.simplecodereview.dto;

public class ChangeStats {
    private int filesChanged;
    private int insertions;
    private int deletions;

    public ChangeStats(int filesChanged, int insertions, int deletions) {
        this.filesChanged = filesChanged;
        this.insertions = insertions;
        this.deletions = deletions;
    }

    // Getter 和 Setter
    public int getFilesChanged() { return filesChanged; }
    public void setFilesChanged(int filesChanged) { this.filesChanged = filesChanged; }

    public int getInsertions() { return insertions; }
    public void setInsertions(int insertions) { this.insertions = insertions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
}