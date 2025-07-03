// DiffController.java - 差异展示API控制器
// src/main/java/com/yourcompany/simplecodereview/controller/DiffController.java
package com.yourcompany.simplecodereview.controller;

import com.yourcompany.simplecodereview.service.GitService;
import com.yourcompany.simplecodereview.service.ChangeService;
import com.yourcompany.simplecodereview.entity.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diff")
@CrossOrigin(origins = "*")
public class DiffController {

    @Autowired
    private GitService gitService;

    @Autowired
    private ChangeService changeService;

    /**
     * 获取变更的完整差异信息
     */
    @GetMapping("/change/{changeId}")
    public ResponseEntity<Map<String, Object>> getChangeDiff(@PathVariable String changeId) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            Map<String, Object> diffData = gitService.getCommitDiff(
                    change.getProjectName(),
                    change.getCommitId()
            );

            // 添加变更基本信息
            diffData.put("changeId", changeId);
            diffData.put("subject", change.getSubject());
            diffData.put("author", change.getAuthorName());
            diffData.put("authorEmail", change.getAuthorEmail());
            diffData.put("commitMessage", change.getCommitMessage());
            diffData.put("branch", change.getBranch());
            diffData.put("status", change.getStatus());
            diffData.put("createdAt", change.getCreatedAt());

            return ResponseEntity.ok(createSuccessResponse("获取差异成功", diffData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更中单个文件的差异
     */
    @GetMapping("/change/{changeId}/file")
    public ResponseEntity<Map<String, Object>> getChangeFileDiff(
            @PathVariable String changeId,
            @RequestParam String filePath) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            Map<String, Object> fileDiff = gitService.getFileDiff(
                    change.getProjectName(),
                    change.getCommitId(),
                    filePath
            );

            // 添加变更基本信息
            fileDiff.put("changeId", changeId);
            fileDiff.put("subject", change.getSubject());

            return ResponseEntity.ok(createSuccessResponse("获取文件差异成功", fileDiff));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取变更涉及的文件列表
     */
    @GetMapping("/change/{changeId}/files")
    public ResponseEntity<Map<String, Object>> getChangeFiles(@PathVariable String changeId) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            List<String> files = gitService.getChangedFiles(
                    change.getProjectName(),
                    change.getCommitId()
            );

            Map<String, Object> response = createSuccessResponse("获取文件列表成功", null);
            response.put("changeId", changeId);
            response.put("files", files);
            response.put("fileCount", files.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 直接根据项目和提交ID获取差异
     */
    @GetMapping("/project/{projectName}/commit/{commitId}")
    public ResponseEntity<Map<String, Object>> getCommitDiff(
            @PathVariable String projectName,
            @PathVariable String commitId) {
        try {
            Map<String, Object> diffData = gitService.getCommitDiff(projectName, commitId);

            return ResponseEntity.ok(createSuccessResponse("获取提交差异成功", diffData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取项目中单个文件在特定提交的差异
     */
    @GetMapping("/project/{projectName}/commit/{commitId}/file")
    public ResponseEntity<Map<String, Object>> getCommitFileDiff(
            @PathVariable String projectName,
            @PathVariable String commitId,
            @RequestParam String filePath) {
        try {
            Map<String, Object> fileDiff = gitService.getFileDiff(projectName, commitId, filePath);

            return ResponseEntity.ok(createSuccessResponse("获取文件差异成功", fileDiff));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取差异统计信息
     */
    @GetMapping("/change/{changeId}/stats")
    public ResponseEntity<Map<String, Object>> getChangeStats(@PathVariable String changeId) {
        try {
            Change change = changeService.getChangeById(changeId)
                    .orElseThrow(() -> new RuntimeException("变更不存在: " + changeId));

            Map<String, Object> diffData = gitService.getCommitDiff(
                    change.getProjectName(),
                    change.getCommitId()
            );

            // 只返回统计信息
            Map<String, Object> stats = (Map<String, Object>) diffData.get("stats");
            stats.put("changeId", changeId);
            stats.put("projectName", change.getProjectName());
            stats.put("commitId", change.getCommitId());

            return ResponseEntity.ok(createSuccessResponse("获取差异统计成功", stats));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取支持的文件类型和语法高亮信息
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        try {
            Map<String, Object> supportedTypes = new HashMap<>();

            // 支持的编程语言
            Map<String, String> languages = new HashMap<>();
            languages.put("java", "Java");
            languages.put("js", "JavaScript");
            languages.put("ts", "TypeScript");
            languages.put("py", "Python");
            languages.put("cpp", "C++");
            languages.put("c", "C");
            languages.put("html", "HTML");
            languages.put("css", "CSS");
            languages.put("sql", "SQL");
            languages.put("xml", "XML");
            languages.put("json", "JSON");
            languages.put("yml", "YAML");
            languages.put("yaml", "YAML");
            languages.put("md", "Markdown");
            languages.put("txt", "Text");

            // 二进制文件类型
            List<String> binaryTypes = List.of(
                    "jpg", "jpeg", "png", "gif", "bmp", "ico",
                    "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                    "zip", "rar", "7z", "tar", "gz",
                    "exe", "dll", "so", "dylib",
                    "jar", "war", "ear"
            );

            supportedTypes.put("languages", languages);
            supportedTypes.put("binaryTypes", binaryTypes);

            return ResponseEntity.ok(createSuccessResponse("获取支持类型成功", supportedTypes));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 检查文件是否为二进制文件
     */
    @GetMapping("/is-binary")
    public ResponseEntity<Map<String, Object>> checkIfBinary(@RequestParam String fileName) {
        try {
            boolean isBinary = isBinaryFile(fileName);
            String fileType = getFileType(fileName);
            String language = getLanguageFromFileName(fileName);

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", fileName);
            fileInfo.put("isBinary", isBinary);
            fileInfo.put("fileType", fileType);
            fileInfo.put("language", language);

            return ResponseEntity.ok(createSuccessResponse("文件类型检查完成", fileInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // 私有辅助方法

    private boolean isBinaryFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        List<String> binaryExtensions = List.of(
                "jpg", "jpeg", "png", "gif", "bmp", "ico",
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "zip", "rar", "7z", "tar", "gz",
                "exe", "dll", "so", "dylib",
                "jar", "war", "ear", "class"
        );
        return binaryExtensions.contains(extension);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private String getFileType(String fileName) {
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            return "unknown";
        }
        return extension.toLowerCase();
    }

    private String getLanguageFromFileName(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "java": return "java";
            case "js": return "javascript";
            case "ts": return "typescript";
            case "py": return "python";
            case "cpp": case "cc": case "cxx": return "cpp";
            case "c": return "c";
            case "h": return "c";
            case "html": case "htm": return "html";
            case "css": return "css";
            case "scss": case "sass": return "scss";
            case "sql": return "sql";
            case "xml": return "xml";
            case "json": return "json";
            case "yml": case "yaml": return "yaml";
            case "md": return "markdown";
            case "sh": return "bash";
            case "php": return "php";
            case "rb": return "ruby";
            case "go": return "go";
            case "rs": return "rust";
            case "kt": return "kotlin";
            case "swift": return "swift";
            default: return "text";
        }
    }

    // 辅助方法
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}