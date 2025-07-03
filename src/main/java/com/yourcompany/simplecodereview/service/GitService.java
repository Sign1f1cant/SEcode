// GitService.java - Git操作服务
// src/main/java/com/yourcompany/simplecodereview/service/GitService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.Project;
import com.yourcompany.simplecodereview.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitService {

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 差异行类型
     */
    public enum DiffLineType {
        CONTEXT,    // 上下文行
        ADDED,      // 新增行
        DELETED,    // 删除行
        HEADER      // 文件头信息
    }

    /**
     * 差异行数据结构
     */
    public static class DiffLine {
        private DiffLineType type;
        private String content;
        private Integer oldLineNumber;
        private Integer newLineNumber;
        private String cssClass;

        public DiffLine(DiffLineType type, String content, Integer oldLineNumber, Integer newLineNumber) {
            this.type = type;
            this.content = content;
            this.oldLineNumber = oldLineNumber;
            this.newLineNumber = newLineNumber;
            this.cssClass = getDiffCssClass(type);
        }

        private String getDiffCssClass(DiffLineType type) {
            switch (type) {
                case ADDED: return "diff-line-added";
                case DELETED: return "diff-line-deleted";
                case HEADER: return "diff-line-header";
                default: return "diff-line-context";
            }
        }

        // Getters
        public DiffLineType getType() { return type; }
        public String getContent() { return content; }
        public Integer getOldLineNumber() { return oldLineNumber; }
        public Integer getNewLineNumber() { return newLineNumber; }
        public String getCssClass() { return cssClass; }
    }

    /**
     * 文件差异数据结构
     */
    public static class FileDiff {
        private String fileName;
        private String oldFileName;
        private String newFileName;
        private String fileStatus; // ADDED, DELETED, MODIFIED, RENAMED
        private List<DiffLine> lines;
        private int addedLines;
        private int deletedLines;
        private boolean isBinary;

        public FileDiff(String fileName) {
            this.fileName = fileName;
            this.lines = new ArrayList<>();
            this.addedLines = 0;
            this.deletedLines = 0;
            this.isBinary = false;
        }

        public void addLine(DiffLine line) {
            this.lines.add(line);
            if (line.getType() == DiffLineType.ADDED) {
                this.addedLines++;
            } else if (line.getType() == DiffLineType.DELETED) {
                this.deletedLines++;
            }
        }

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getOldFileName() { return oldFileName; }
        public void setOldFileName(String oldFileName) { this.oldFileName = oldFileName; }

        public String getNewFileName() { return newFileName; }
        public void setNewFileName(String newFileName) { this.newFileName = newFileName; }

        public String getFileStatus() { return fileStatus; }
        public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }

        public List<DiffLine> getLines() { return lines; }
        public void setLines(List<DiffLine> lines) { this.lines = lines; }

        public int getAddedLines() { return addedLines; }
        public void setAddedLines(int addedLines) { this.addedLines = addedLines; }

        public int getDeletedLines() { return deletedLines; }
        public void setDeletedLines(int deletedLines) { this.deletedLines = deletedLines; }

        public boolean isBinary() { return isBinary; }
        public void setBinary(boolean binary) { isBinary = binary; }
    }

    /**
     * 获取提交的差异信息
     */
    public Map<String, Object> getCommitDiff(String projectName, String commitId) {
        try {
            Project project = projectRepository.findById(projectName)
                    .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

            String repoPath = getRepositoryPath(project);

            // 获取差异数据
            List<FileDiff> fileDiffs = getGitDiff(repoPath, commitId);

            // 统计信息
            Map<String, Object> stats = calculateDiffStats(fileDiffs);

            Map<String, Object> result = new HashMap<>();
            result.put("projectName", projectName);
            result.put("commitId", commitId);
            result.put("files", fileDiffs);
            result.put("stats", stats);

            return result;

        } catch (Exception e) {
            // 如果Git操作失败，返回模拟数据
            return generateMockDiff(projectName, commitId);
        }
    }

    /**
     * 获取单个文件的差异
     */
    public Map<String, Object> getFileDiff(String projectName, String commitId, String filePath) {
        try {
            Project project = projectRepository.findById(projectName)
                    .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

            String repoPath = getRepositoryPath(project);
            FileDiff fileDiff = getGitFileDiff(repoPath, commitId, filePath);

            Map<String, Object> result = new HashMap<>();
            result.put("projectName", projectName);
            result.put("commitId", commitId);
            result.put("filePath", filePath);
            result.put("diff", fileDiff);

            return result;

        } catch (Exception e) {
            // 如果Git操作失败，返回模拟数据
            return generateMockFileDiff(projectName, commitId, filePath);
        }
    }

    /**
     * 获取变更的文件列表
     */
    public List<String> getChangedFiles(String projectName, String commitId) {
        try {
            Project project = projectRepository.findById(projectName)
                    .orElseThrow(() -> new RuntimeException("项目不存在: " + projectName));

            String repoPath = getRepositoryPath(project);
            return getGitChangedFiles(repoPath, commitId);

        } catch (Exception e) {
            // 如果Git操作失败，返回模拟数据
            return Arrays.asList(
                    "src/main/java/com/example/TestService.java",
                    "src/main/java/com/example/TestController.java",
                    "src/test/java/com/example/TestServiceTest.java",
                    "README.md"
            );
        }
    }

    // Git操作相关的私有方法

    private String getRepositoryPath(Project project) {
        String gitPath = project.getGitPath();

        // 如果是远程仓库URL，转换为本地路径
        if (gitPath.startsWith("http") || gitPath.startsWith("git@")) {
            // 简化处理：假设本地有克隆的仓库
            String repoName = extractRepoName(gitPath);
            return "./git-repos/" + repoName;
        }

        return gitPath;
    }

    private String extractRepoName(String gitUrl) {
        String[] parts = gitUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.replace(".git", "");
    }

    private List<FileDiff> getGitDiff(String repoPath, String commitId) throws Exception {
        List<FileDiff> fileDiffs = new ArrayList<>();

        // 执行git diff命令
        ProcessBuilder pb = new ProcessBuilder("git", "show", "--name-status", commitId);
        pb.directory(new File(repoPath));

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");
            if (parts.length >= 2) {
                String status = parts[0];
                String fileName = parts[1];

                FileDiff fileDiff = new FileDiff(fileName);
                fileDiff.setFileStatus(getFileStatus(status));

                // 获取文件详细差异
                List<DiffLine> diffLines = getGitFileDiffLines(repoPath, commitId, fileName);
                diffLines.forEach(fileDiff::addLine);

                fileDiffs.add(fileDiff);
            }
        }

        process.waitFor();
        return fileDiffs;
    }

    private FileDiff getGitFileDiff(String repoPath, String commitId, String filePath) throws Exception {
        FileDiff fileDiff = new FileDiff(filePath);

        List<DiffLine> diffLines = getGitFileDiffLines(repoPath, commitId, filePath);
        diffLines.forEach(fileDiff::addLine);

        return fileDiff;
    }

    private List<DiffLine> getGitFileDiffLines(String repoPath, String commitId, String filePath) throws Exception {
        List<DiffLine> diffLines = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder("git", "show", commitId, "--", filePath);
        pb.directory(new File(repoPath));

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int oldLineNum = 1;
        int newLineNum = 1;
        Pattern hunkPattern = Pattern.compile("@@\\s+-([0-9]+)(?:,([0-9]+))?\\s+\\+([0-9]+)(?:,([0-9]+))?\\s+@@");

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("diff --git") || line.startsWith("index") ||
                    line.startsWith("+++") || line.startsWith("---")) {
                diffLines.add(new DiffLine(DiffLineType.HEADER, line, null, null));
            } else if (line.startsWith("@@")) {
                Matcher matcher = hunkPattern.matcher(line);
                if (matcher.find()) {
                    oldLineNum = Integer.parseInt(matcher.group(1));
                    newLineNum = Integer.parseInt(matcher.group(3));
                }
                diffLines.add(new DiffLine(DiffLineType.HEADER, line, null, null));
            } else if (line.startsWith("+")) {
                diffLines.add(new DiffLine(DiffLineType.ADDED, line.substring(1), null, newLineNum++));
            } else if (line.startsWith("-")) {
                diffLines.add(new DiffLine(DiffLineType.DELETED, line.substring(1), oldLineNum++, null));
            } else {
                diffLines.add(new DiffLine(DiffLineType.CONTEXT, line, oldLineNum++, newLineNum++));
            }
        }

        process.waitFor();
        return diffLines;
    }

    private List<String> getGitChangedFiles(String repoPath, String commitId) throws Exception {
        List<String> files = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder("git", "show", "--name-only", commitId);
        pb.directory(new File(repoPath));

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty() && !line.startsWith("commit")) {
                files.add(line.trim());
            }
        }

        process.waitFor();
        return files;
    }

    private String getFileStatus(String gitStatus) {
        switch (gitStatus.charAt(0)) {
            case 'A': return "ADDED";
            case 'D': return "DELETED";
            case 'M': return "MODIFIED";
            case 'R': return "RENAMED";
            default: return "MODIFIED";
        }
    }

    private Map<String, Object> calculateDiffStats(List<FileDiff> fileDiffs) {
        Map<String, Object> stats = new HashMap<>();

        int totalFiles = fileDiffs.size();
        int totalAdditions = fileDiffs.stream().mapToInt(FileDiff::getAddedLines).sum();
        int totalDeletions = fileDiffs.stream().mapToInt(FileDiff::getDeletedLines).sum();

        long addedFiles = fileDiffs.stream().filter(f -> "ADDED".equals(f.getFileStatus())).count();
        long deletedFiles = fileDiffs.stream().filter(f -> "DELETED".equals(f.getFileStatus())).count();
        long modifiedFiles = fileDiffs.stream().filter(f -> "MODIFIED".equals(f.getFileStatus())).count();

        stats.put("totalFiles", totalFiles);
        stats.put("totalAdditions", totalAdditions);
        stats.put("totalDeletions", totalDeletions);
        stats.put("addedFiles", addedFiles);
        stats.put("deletedFiles", deletedFiles);
        stats.put("modifiedFiles", modifiedFiles);

        return stats;
    }

    // 模拟数据生成方法（当Git操作失败时使用）

    private Map<String, Object> generateMockDiff(String projectName, String commitId) {
        List<FileDiff> mockFiles = new ArrayList<>();

        // 模拟Java文件修改
        FileDiff javaFile = new FileDiff("src/main/java/com/example/UserService.java");
        javaFile.setFileStatus("MODIFIED");
        javaFile.addLine(new DiffLine(DiffLineType.HEADER, "@@ -15,7 +15,9 @@ public class UserService {", null, null));
        javaFile.addLine(new DiffLine(DiffLineType.CONTEXT, "    public User findById(Long id) {", 15, 15));
        javaFile.addLine(new DiffLine(DiffLineType.DELETED, "        return userRepository.findById(id);", 16, null));
        javaFile.addLine(new DiffLine(DiffLineType.ADDED, "        if (id == null) {", null, 16));
        javaFile.addLine(new DiffLine(DiffLineType.ADDED, "            throw new IllegalArgumentException(\"ID不能为空\");", null, 17));
        javaFile.addLine(new DiffLine(DiffLineType.ADDED, "        }", null, 18));
        javaFile.addLine(new DiffLine(DiffLineType.ADDED, "        return userRepository.findById(id).orElse(null);", null, 19));
        javaFile.addLine(new DiffLine(DiffLineType.CONTEXT, "    }", 17, 20));

        mockFiles.add(javaFile);

        // 模拟新增文件
        FileDiff newFile = new FileDiff("src/main/java/com/example/ValidationUtils.java");
        newFile.setFileStatus("ADDED");
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "package com.example;", null, 1));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "", null, 2));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "public class ValidationUtils {", null, 3));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "    public static boolean isValidId(Long id) {", null, 4));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "        return id != null && id > 0;", null, 5));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "    }", null, 6));
        newFile.addLine(new DiffLine(DiffLineType.ADDED, "}", null, 7));

        mockFiles.add(newFile);

        Map<String, Object> stats = calculateDiffStats(mockFiles);

        Map<String, Object> result = new HashMap<>();
        result.put("projectName", projectName);
        result.put("commitId", commitId);
        result.put("files", mockFiles);
        result.put("stats", stats);
        result.put("isMockData", true);

        return result;
    }

    private Map<String, Object> generateMockFileDiff(String projectName, String commitId, String filePath) {
        FileDiff mockFile = new FileDiff(filePath);
        mockFile.setFileStatus("MODIFIED");

        // 生成模拟的差异内容
        mockFile.addLine(new DiffLine(DiffLineType.HEADER, "diff --git a/" + filePath + " b/" + filePath, null, null));
        mockFile.addLine(new DiffLine(DiffLineType.HEADER, "@@ -10,6 +10,8 @@", null, null));
        mockFile.addLine(new DiffLine(DiffLineType.CONTEXT, "public class Example {", 10, 10));
        mockFile.addLine(new DiffLine(DiffLineType.CONTEXT, "    private String name;", 11, 11));
        mockFile.addLine(new DiffLine(DiffLineType.ADDED, "    private Long id;", null, 12));
        mockFile.addLine(new DiffLine(DiffLineType.ADDED, "", null, 13));
        mockFile.addLine(new DiffLine(DiffLineType.CONTEXT, "    public String getName() {", 12, 14));
        mockFile.addLine(new DiffLine(DiffLineType.CONTEXT, "        return name;", 13, 15));
        mockFile.addLine(new DiffLine(DiffLineType.CONTEXT, "    }", 14, 16));

        Map<String, Object> result = new HashMap<>();
        result.put("projectName", projectName);
        result.put("commitId", commitId);
        result.put("filePath", filePath);
        result.put("diff", mockFile);
        result.put("isMockData", true);

        return result;
    }
}