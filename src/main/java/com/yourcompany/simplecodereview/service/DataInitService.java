// src/main/java/com/yourcompany/simplecodereview/service/DataInitService.java
package com.yourcompany.simplecodereview.service;

import com.yourcompany.simplecodereview.entity.*;
import com.yourcompany.simplecodereview.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataInitService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired(required = false)
    private ReviewTemplateRepository reviewTemplateRepository;

    // 默认管理员配置
    @Value("${app.default-admin.username:admin}")
    private String adminUsername;

    @Value("${app.default-admin.email:admin@localhost}")
    private String adminEmail;

    @Value("${app.default-admin.password:admin123}")
    private String adminPassword;

    @Value("${app.default-admin.display-name:系统管理员}")
    private String adminDisplayName;

    // 默认用户配置
    @Value("${app.default-users.enabled:true}")
    private boolean createDefaultUsers;

    @Value("${app.default-users.reviewer1.username:reviewer1}")
    private String reviewer1Username;

    @Value("${app.default-users.reviewer1.email:reviewer1@localhost}")
    private String reviewer1Email;

    @Value("${app.default-users.reviewer1.password:review123}")
    private String reviewer1Password;

    @Value("${app.default-users.reviewer1.display-name:审查者1}")
    private String reviewer1DisplayName;

    @Value("${app.default-users.reviewer2.username:reviewer2}")
    private String reviewer2Username;

    @Value("${app.default-users.reviewer2.email:reviewer2@localhost}")
    private String reviewer2Email;

    @Value("${app.default-users.reviewer2.password:review123}")
    private String reviewer2Password;

    @Value("${app.default-users.reviewer2.display-name:审查者2}")
    private String reviewer2DisplayName;

    @Value("${app.default-users.developer1.username:developer1}")
    private String developer1Username;

    @Value("${app.default-users.developer1.email:developer1@localhost}")
    private String developer1Email;

    @Value("${app.default-users.developer1.password:dev123}")
    private String developer1Password;

    @Value("${app.default-users.developer1.display-name:开发者1}")
    private String developer1DisplayName;

    @PostConstruct
    public void initializeData() {
        initializeDefaultUsers();
        if (createDefaultUsers) {
            initializeTestUsers();
            initializeTestProjects();
        }
        initializeReviewTemplates();
        logInitializationResults();
    }

    /**
     * 初始化默认管理员用户
     */
    private void initializeDefaultUsers() {
        try {
            // 创建默认管理员
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = new User(adminUsername, adminEmail, adminPassword, adminDisplayName);
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ 默认管理员用户创建成功: " + adminUsername);
            } else {
                System.out.println("ℹ️ 默认管理员用户已存在: " + adminUsername);
            }
        } catch (Exception e) {
            System.err.println("❌ 创建默认管理员失败: " + e.getMessage());
        }
    }

    /**
     * 初始化测试用户（仅在开发环境）
     */
    private void initializeTestUsers() {
        try {
            // 创建审查者1
            if (!userRepository.existsByUsername(reviewer1Username)) {
                User reviewer1 = new User(reviewer1Username, reviewer1Email, reviewer1Password, reviewer1DisplayName);
                reviewer1.setRole(UserRole.REVIEWER);
                userRepository.save(reviewer1);
                System.out.println("✅ 测试审查者1创建成功: " + reviewer1Username);
            }

            // 创建审查者2
            if (!userRepository.existsByUsername(reviewer2Username)) {
                User reviewer2 = new User(reviewer2Username, reviewer2Email, reviewer2Password, reviewer2DisplayName);
                reviewer2.setRole(UserRole.REVIEWER);
                userRepository.save(reviewer2);
                System.out.println("✅ 测试审查者2创建成功: " + reviewer2Username);
            }

            // 创建开发者1
            if (!userRepository.existsByUsername(developer1Username)) {
                User developer1 = new User(developer1Username, developer1Email, developer1Password, developer1DisplayName);
                developer1.setRole(UserRole.DEVELOPER);
                userRepository.save(developer1);
                System.out.println("✅ 测试开发者1创建成功: " + developer1Username);
            }

        } catch (Exception e) {
            System.err.println("❌ 创建测试用户失败: " + e.getMessage());
        }
    }

    /**
     * 初始化测试项目
     */
    private void initializeTestProjects() {
        try {
            // 获取管理员用户ID
            User admin = userRepository.findByUsername(adminUsername).orElse(null);
            if (admin == null) {
                System.err.println("❌ 无法找到管理员用户，跳过项目初始化");
                return;
            }

            // 创建示例项目1
            if (!projectRepository.existsById("demo-project")) {
                Project demoProject = new Project(
                        "demo-project",
                        "演示项目 - 用于测试审查功能",
                        "https://github.com/example/demo-project.git",
                        admin.getId()
                );
                projectRepository.save(demoProject);
                System.out.println("✅ 示例项目创建成功: demo-project");
            }

            // 创建示例项目2
            if (!projectRepository.existsById("spring-boot-app")) {
                Project springProject = new Project(
                        "spring-boot-app",
                        "Spring Boot应用示例",
                        "https://github.com/spring-projects/spring-boot.git",
                        admin.getId()
                );
                projectRepository.save(springProject);
                System.out.println("✅ 示例项目创建成功: spring-boot-app");
            }

            // 创建本地项目示例
            if (!projectRepository.existsById("local-project")) {
                Project localProject = new Project(
                        "local-project",
                        "本地Git仓库示例",
                        "./git-repos/local-project",
                        admin.getId()
                );
                projectRepository.save(localProject);
                System.out.println("✅ 本地项目创建成功: local-project");
            }

        } catch (Exception e) {
            System.err.println("❌ 创建测试项目失败: " + e.getMessage());
        }
    }

    /**
     * 初始化审查模板
     */
    private void initializeReviewTemplates() {
        if (reviewTemplateRepository == null) {
            System.out.println("ℹ️ ReviewTemplateRepository 未配置，跳过模板初始化");
            return;
        }

        try {
            // 获取管理员用户ID
            User admin = userRepository.findByUsername(adminUsername).orElse(null);
            if (admin == null) {
                System.err.println("❌ 无法找到管理员用户，跳过审查模板初始化");
                return;
            }

            // 创建默认代码审查模板
            if (!reviewTemplateRepository.existsByName("默认代码审查")) {
                ReviewTemplate defaultTemplate = new ReviewTemplate(
                        "默认代码审查",
                        "标准的代码审查模板",
                        null,
                        "[\"代码风格检查\", \"逻辑正确性验证\", \"性能考虑\", \"安全性检查\", \"测试覆盖率\", \"文档完整性\"]",
                        admin.getId()
                );
                reviewTemplateRepository.save(defaultTemplate);
                System.out.println("✅ 默认审查模板创建成功");
            }

            // 创建安全审查模板
            if (!reviewTemplateRepository.existsByName("安全审查模板")) {
                ReviewTemplate securityTemplate = new ReviewTemplate(
                        "安全审查模板",
                        "专注于安全性的审查模板",
                        null,
                        "[\"输入验证检查\", \"SQL注入防护\", \"XSS攻击防护\", \"权限验证\", \"敏感数据处理\", \"加密算法使用\", \"依赖安全性\"]",
                        admin.getId()
                );
                reviewTemplateRepository.save(securityTemplate);
                System.out.println("✅ 安全审查模板创建成功");
            }

            // 创建性能优化审查模板
            if (!reviewTemplateRepository.existsByName("性能优化审查")) {
                ReviewTemplate performanceTemplate = new ReviewTemplate(
                        "性能优化审查",
                        "专注于性能的审查模板",
                        null,
                        "[\"算法复杂度分析\", \"数据库查询优化\", \"缓存策略\", \"资源释放\", \"并发处理\", \"内存使用\", \"网络请求优化\"]",
                        admin.getId()
                );
                reviewTemplateRepository.save(performanceTemplate);
                System.out.println("✅ 性能审查模板创建成功");
            }

        } catch (Exception e) {
            System.err.println("❌ 创建审查模板失败: " + e.getMessage());
        }
    }

    /**
     * 记录初始化结果
     */
    private void logInitializationResults() {
        try {
            long userCount = userRepository.count();
            long projectCount = projectRepository.count();
            long adminCount = userRepository.findByRole(UserRole.ADMIN).size();
            long reviewerCount = userRepository.findByRole(UserRole.REVIEWER).size();
            long developerCount = userRepository.findByRole(UserRole.DEVELOPER).size();

            System.out.println("\n" + "=".repeat(50));
            System.out.println("📊 数据初始化完成统计:");
            System.out.println("=".repeat(50));
            System.out.println("👥 用户总数: " + userCount);
            System.out.println("   - 管理员: " + adminCount);
            System.out.println("   - 审查者: " + reviewerCount);
            System.out.println("   - 开发者: " + developerCount);
            System.out.println("🏗️  项目总数: " + projectCount);

            if (reviewTemplateRepository != null) {
                long templateCount = reviewTemplateRepository.count();
                System.out.println("📋 审查模板: " + templateCount);
            }

            System.out.println("=".repeat(50));

            if (createDefaultUsers) {
                System.out.println("🚀 系统已准备就绪，可以开始测试审查功能!");
                System.out.println("📝 测试账号:");
                System.out.println("   - 管理员: " + adminUsername + " / " + adminPassword);
                System.out.println("   - 审查者1: " + reviewer1Username + " / " + reviewer1Password);
                System.out.println("   - 审查者2: " + reviewer2Username + " / " + reviewer2Password);
                System.out.println("   - 开发者1: " + developer1Username + " / " + developer1Password);
                System.out.println("🌐 H2控制台: http://localhost:8080/h2-console");
                System.out.println("   - JDBC URL: jdbc:h2:file:./data/codereview");
                System.out.println("   - 用户名: sa");
                System.out.println("   - 密码: (空)");
            } else {
                System.out.println("✅ 生产环境模式 - 仅创建了管理员账号");
            }
            System.out.println("=".repeat(50) + "\n");

        } catch (Exception e) {
            System.err.println("❌ 获取初始化统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取初始化状态（供其他服务使用）
     */
    public boolean isInitialized() {
        return userRepository.existsByUsername(adminUsername);
    }

    /**
     * 获取默认管理员用户
     */
    public User getDefaultAdmin() {
        return userRepository.findByUsername(adminUsername).orElse(null);
    }

    /**
     * 获取所有审查者用户
     */
    public java.util.List<User> getDefaultReviewers() {
        return userRepository.findByRole(UserRole.REVIEWER);
    }
}