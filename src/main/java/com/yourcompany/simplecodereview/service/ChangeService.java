// src/main/java/com/yourcompany/simplecodereview/service/ChangeService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.*;
import com.yourcompany.simplecodereview.repository.*;
import com.yourcompany.simplecodereview.dto.CommitInfo;
import com.yourcompany.simplecodereview.dto.ChangeStats;
import com.yourcompany.simplecodereview.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ChangeService {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GitUtil gitUtil;

    /**
     * 创建新的变更
     */
    public Change createChange(String projectName, String branch, String commitId, Long submitterId) {
        // 验证项目是否存在
        Project project = projectRepository.findById(projectName)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

        // 验证用户是否存在
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + submitterId));

        // 检查commit是否已经存在
        if (changeRepository.existsByCommitId(commitId)) {
            throw new RuntimeException("该提交已经存在变更记录: " + commitId);
        }

        // 验证commit格式（简单验证）
        if (commitId == null || commitId.length() < 7) {
            throw new RuntimeException("无效的提交ID格式");
        }

        try {
            // 获取提交信息
            CommitInfo commitInfo = gitUtil.getCommitInfo(project.getGitPath(), commitId);

            // 生成变更ID
            String changeId = generateChangeId(projectName);

            // 创建变更对象
            Change change = new Change(
                    changeId,
                    projectName,
                    branch,
                    commitInfo.getShortMessage(),
                    commitId,
                    submitterId
            );

            // 设置提交信息
            change.setCommitMessage(commitInfo.getFullMessage());
            change.setAuthorName(commitInfo.getAuthorName());
            change.setAuthorEmail(commitInfo.getAuthorEmail());

            // 获取变更统计
            try {
                ChangeStats stats = gitUtil.getChangeStats(project.getGitPath(), commitId);
                change.setFilesChanged(stats.getFilesChanged());
                change.setInsertions(stats.getInsertions());
                change.setDeletions(stats.getDeletions());
            } catch (Exception e) {
                // 如果获取统计失败，使用默认值
                change.setFilesChanged(1);
                change.setInsertions(0);
                change.setDeletions(0);
            }

            return changeRepository.save(change);

        } catch (Exception e) {
            throw new RuntimeException("创建变更失败: " + e.getMessage());
        }
    }

    /**
     * 根据提交信息自动创建变更（简化版本）
     */
    public Change createChangeFromCommitInfo(String projectName, String branch,
                                             String commitId, String commitMessage,
                                             String authorName, String authorEmail,
                                             Long submitterId) {
        // 验证项目是否存在
        Project project = projectRepository.findById(projectName)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

        // 验证用户是否存在
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + submitterId));

        // 检查commit是否已经存在
        if (changeRepository.existsByCommitId(commitId)) {
            throw new RuntimeException("该提交已经存在变更记录: " + commitId);
        }

        // 生成变更ID
        String changeId = generateChangeId(projectName);

        // 从提交消息中提取标题
        String subject = commitMessage.split("\n")[0];
        if (subject.length() > 100) {
            subject = subject.substring(0, 97) + "...";
        }

        // 创建变更对象
        Change change = new Change(changeId, projectName, branch, subject, commitId, submitterId);
        change.setCommitMessage(commitMessage);
        change.setAuthorName(authorName);
        change.setAuthorEmail(authorEmail);

        // 设置默认统计
        change.setFilesChanged(1);
        change.setInsertions(10);
        change.setDeletions(2);

        return changeRepository.save(change);
    }

    /**
     * 获取变更详情
     */
    public Optional<Change> getChangeById(String changeId) {
        return changeRepository.findById(changeId);
    }

    /**
     * 获取项目的所有变更
     */
    public List<Change> getChangesByProject(String projectName) {
        return changeRepository.findByProjectNameOrderByCreatedAtDesc(projectName);
    }

    /**
     * 获取指定状态的变更
     */
    public List<Change> getChangesByStatus(ChangeStatus status) {
        return changeRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 获取项目中指定状态的变更
     */
    public List<Change> getChangesByProjectAndStatus(String projectName, ChangeStatus status) {
        return changeRepository.findByProjectNameAndStatusOrderByCreatedAtDesc(projectName, status);
    }

    /**
     * 获取用户提交的变更
     */
    public List<Change> getChangesBySubmitter(Long submitterId) {
        return changeRepository.findBySubmitterIdOrderByCreatedAtDesc(submitterId);
    }

    /**
     * 获取作者的变更
     */
    public List<Change> getChangesByAuthor(String authorEmail) {
        return changeRepository.findByAuthorEmailOrderByCreatedAtDesc(authorEmail);
    }

    /**
     * 更新变更状态
     */
    public Change updateChangeStatus(String changeId, ChangeStatus newStatus) {
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

        // 检查状态转换是否合法
        if (!isValidStatusTransition(change.getStatus(), newStatus)) {
            throw new RuntimeException("无效的状态转换: " + change.getStatus() + " -> " + newStatus);
        }

        change.setStatus(newStatus);
        return changeRepository.save(change);
    }

    /**
     * 废弃变更
     */
    public Change abandonChange(String changeId, String reason) {
        Change change = updateChangeStatus(changeId, ChangeStatus.ABANDONED);
        // 这里可以记录废弃原因，需要扩展Change实体
        return change;
    }

    /**
     * 合并变更
     */
    public Change mergeChange(String changeId) {
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

        if (change.getStatus() != ChangeStatus.OPEN) {
            throw new RuntimeException("只有开放状态的变更才能合并");
        }

        // TODO: 这里应该检查审查状态
        // if (!canMergeChange(changeId)) {
        //     throw new RuntimeException("变更未通过审查，无法合并");
        // }

        // TODO: 这里应该实际执行Git合并操作
        // 现在只是更新状态
        return updateChangeStatus(changeId, ChangeStatus.MERGED);
    }

    /**
     * 获取变更涉及的文件列表
     */
    public List<String> getChangedFiles(String changeId) {
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

        Project project = projectRepository.findById(change.getProjectName())
                .orElseThrow(() -> new RuntimeException("项目不存在: " + change.getProjectName()));

        try {
            return gitUtil.getChangedFiles(project.getGitPath(), change.getCommitId());
        } catch (Exception e) {
            throw new RuntimeException("获取变更文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的开放变更数量
     */
    public long getOpenChangesCount(String projectName) {
        return changeRepository.countOpenChangesByProject(projectName);
    }

    /**
     * 获取用户的开放变更数量
     */
    public long getOpenChangesCountBySubmitter(Long submitterId) {
        return changeRepository.countOpenChangesBySubmitter(submitterId);
    }

    /**
     * 从token解析用户ID
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("user_")) {
            throw new RuntimeException("无效的token");
        }

        try {
            return Long.parseLong(token.substring(5));
        } catch (NumberFormatException e) {
            throw new RuntimeException("token格式错误");
        }
    }

    // 私有辅助方法

    /**
     * 生成变更ID
     */
    private String generateChangeId(String projectName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return projectName + "_" + timestamp + "_" + random;
    }

    /**
     * 检查状态转换是否合法
     */
    private boolean isValidStatusTransition(ChangeStatus current, ChangeStatus target) {
        switch (current) {
            case DRAFT:
                return target == ChangeStatus.OPEN || target == ChangeStatus.ABANDONED;
            case OPEN:
                return target == ChangeStatus.MERGED || target == ChangeStatus.ABANDONED || target == ChangeStatus.DRAFT;
            case MERGED:
            case ABANDONED:
                return false; // 已终结状态不能再转换
            default:
                return false;
        }
    }
}