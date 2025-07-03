// 在 GitUtil.java 中添加以下方法
package com.yourcompany.simplecodereview.util;

import com.yourcompany.simplecodereview.dto.ChangeStats;
import com.yourcompany.simplecodereview.dto.CommitInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Component
public class GitUtil {

    // ... 保留原有方法 ...

    /**
     * 获取提交信息
     */
    public CommitInfo getCommitInfo(String gitDir, String commitId) throws IOException {
        // 对于远程仓库，我们暂时返回模拟数据
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return new CommitInfo(
                    commitId,
                    "Remote Author",
                    "author@example.com",
                    "Remote commit: " + commitId.substring(0, 8),
                    "This is a commit from remote repository",
                    new Date()
            );
        }

        try (Git git = getGit(gitDir)) {
            Repository repository = git.getRepository();
            ObjectId objectId = ObjectId.fromString(commitId);

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(objectId);

                PersonIdent author = commit.getAuthorIdent();
                String message = commit.getFullMessage();
                String shortMessage = commit.getShortMessage();

                return new CommitInfo(
                        commitId,
                        author.getName(),
                        author.getEmailAddress(),
                        shortMessage,
                        message,
                        new Date(author.getWhen().getTime())
                );
            }
        } catch (Exception e) {
            throw new IOException("获取提交信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查提交是否存在（简化版本）
     */
    public boolean commitExists(String gitDir, String commitId) {
        // 对于远程仓库，我们假设提交存在
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return commitId != null && commitId.length() >= 7;
        }

        try (Git git = getGit(gitDir)) {
            Repository repository = git.getRepository();
            ObjectId objectId = ObjectId.fromString(commitId);
            return repository.getObjectDatabase().has(objectId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取变更文件列表（简化版本）
     */
    public List<String> getChangedFiles(String gitDir, String commitId) throws IOException {
        // 对于远程仓库，返回模拟数据
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            List<String> files = new ArrayList<>();
            files.add("src/main/java/Example.java");
            files.add("README.md");
            return files;
        }

        // 对于本地仓库，这里需要实现具体的差异比较逻辑
        // 暂时返回示例数据
        List<String> files = new ArrayList<>();
        files.add("modified_file.txt");
        return files;
    }

    /**
     * 计算变更统计（简化版本）
     */
    public ChangeStats getChangeStats(String gitDir, String commitId) throws IOException {
        // 对于远程仓库，返回模拟统计
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return new ChangeStats(2, 15, 3); // 2个文件，15行新增，3行删除
        }

        // 对于本地仓库，这里需要实现具体的统计逻辑
        // 暂时返回示例数据
        return new ChangeStats(1, 10, 2);
    }

    // 保留原有的所有方法...
    public Repository openRepository(String gitDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File gitDirectory = new File(gitDir);

        if (gitDirectory.getName().equals(".git")) {
            builder.setGitDir(gitDirectory);
        } else {
            builder.setGitDir(new File(gitDirectory, ".git"));
        }

        return builder.readEnvironment().findGitDir().build();
    }

    public Git getGit(String gitDir) throws IOException {
        Repository repository = openRepository(gitDir);
        return new Git(repository);
    }

    public String getDefaultBranch(String gitDir) throws IOException {
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return "main"; // 远程仓库默认分支
        }

        try (Repository repository = openRepository(gitDir)) {
            String branch = repository.getBranch();
            return branch != null ? branch : "main";
        }
    }

    public List<String> getBranches(String gitDir) throws IOException {
        List<String> branchNames = new ArrayList<>();

        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            // 远程仓库的默认分支
            branchNames.add("main");
            branchNames.add("develop");
            return branchNames;
        }

        try (Git git = getGit(gitDir)) {
            List<Ref> branches = git.branchList().call();
            for (Ref branch : branches) {
                String branchName = branch.getName();
                if (branchName.startsWith("refs/heads/")) {
                    branchName = branchName.substring("refs/heads/".length());
                }
                branchNames.add(branchName);
            }
        } catch (Exception e) {
            throw new IOException("获取分支列表失败: " + e.getMessage());
        }

        return branchNames;
    }

    public boolean repositoryExists(String gitDir) {
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return true; // 假设远程仓库存在
        }

        try {
            Repository repository = openRepository(gitDir);
            boolean exists = repository.getObjectDatabase().exists();
            repository.close();
            return exists;
        } catch (IOException e) {
            return false;
        }
    }

    public String getRepositoryInfo(String gitDir) {
        if (gitDir.startsWith("http") || gitDir.startsWith("git@")) {
            return "Remote Repository: " + gitDir + "\nBranch: main\nStatus: Available";
        }

        try (Repository repository = openRepository(gitDir)) {
            StringBuilder info = new StringBuilder();
            info.append("Repository: ").append(repository.getDirectory().getAbsolutePath()).append("\n");
            info.append("Branch: ").append(repository.getBranch()).append("\n");
            info.append("Object Database: ").append(repository.getObjectDatabase().exists() ? "OK" : "Not Found");
            return info.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}