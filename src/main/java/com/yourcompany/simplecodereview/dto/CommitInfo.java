// src/main/java/com/yourcompany/simplecodereview/dto/CommitInfo.java
package com.yourcompany.simplecodereview.dto;

import java.util.Date;

public class CommitInfo {
    private String commitId;
    private String authorName;
    private String authorEmail;
    private String shortMessage;
    private String fullMessage;
    private Date commitDate;

    public CommitInfo(String commitId, String authorName, String authorEmail,
                      String shortMessage, String fullMessage, Date commitDate) {
        this.commitId = commitId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.commitDate = commitDate;
    }

    // Getter 和 Setter
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getShortMessage() { return shortMessage; }
    public void setShortMessage(String shortMessage) { this.shortMessage = shortMessage; }

    public String getFullMessage() { return fullMessage; }
    public void setFullMessage(String fullMessage) { this.fullMessage = fullMessage; }

    public Date getCommitDate() { return commitDate; }
    public void setCommitDate(Date commitDate) { this.commitDate = commitDate; }
}