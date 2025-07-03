-- 修复的data.sql文件
-- src/main/resources/data.sql

-- noinspection SqlNoDataSourceInspectionForFile
-- noinspection SqlDialectInspectionForFile

-- 这个文件现在只包含视图和索引，不包含表数据
-- 表数据的初始化通过DataInitService.java在应用启动后处理

-- ========================================
-- 创建有用的视图
-- ========================================

-- 用户统计视图
DROP VIEW IF EXISTS user_stats;
CREATE VIEW user_stats AS
SELECT
    u.id,
    u.username,
    u.display_name,
    u.role,
    u.created_at,
    u.last_login,
    COALESCE(submitted.count, 0) as submitted_changes,
    COALESCE(reviewed.count, 0) as reviews_done,
    COALESCE(comments.count, 0) as comments_made,
    COALESCE(pending.count, 0) as pending_reviews
FROM users u
         LEFT JOIN (
    SELECT submitter_id, COUNT(*) as count
    FROM changes
    GROUP BY submitter_id
) submitted ON u.id = submitted.submitter_id
         LEFT JOIN (
    SELECT reviewer_id, COUNT(*) as count
    FROM reviews
    WHERE status = 'COMPLETED' AND is_active = TRUE
    GROUP BY reviewer_id
) reviewed ON u.id = reviewed.reviewer_id
         LEFT JOIN (
    SELECT author_id, COUNT(*) as count
    FROM comments
    GROUP BY author_id
) comments ON u.id = comments.author_id
         LEFT JOIN (
    SELECT reviewer_id, COUNT(*) as count
    FROM reviews
    WHERE status = 'PENDING' AND is_active = TRUE
    GROUP BY reviewer_id
) pending ON u.id = pending.reviewer_id;

-- 项目活动统计视图
DROP VIEW IF EXISTS project_activity;
CREATE VIEW project_activity AS
SELECT
    p.name,
    p.description,
    p.created_at,
    COALESCE(changes.total, 0) as total_changes,
    COALESCE(changes.open, 0) as open_changes,
    COALESCE(changes.merged, 0) as merged_changes,
    COALESCE(reviews.total, 0) as total_reviews,
    COALESCE(reviews.pending, 0) as pending_reviews,
    COALESCE(comments.total, 0) as total_comments,
    changes.last_change
FROM projects p
         LEFT JOIN (
    SELECT
        project_name,
        COUNT(*) as total,
        COUNT(CASE WHEN status = 'OPEN' THEN 1 END) as open,
        COUNT(CASE WHEN status = 'MERGED' THEN 1 END) as merged,
        MAX(created_at) as last_change
    FROM changes
    GROUP BY project_name
) changes ON p.name = changes.project_name
         LEFT JOIN (
    SELECT
        c.project_name,
        COUNT(r.id) as total,
        COUNT(CASE WHEN r.status = 'PENDING' AND r.is_active = TRUE THEN 1 END) as pending
    FROM changes c
             LEFT JOIN reviews r ON c.change_id = r.change_id
    GROUP BY c.project_name
) reviews ON p.name = reviews.project_name
         LEFT JOIN (
    SELECT
        c.project_name,
        COUNT(cm.id) as total
    FROM changes c
             LEFT JOIN comments cm ON c.change_id = cm.change_id
    GROUP BY c.project_name
) comments ON p.name = comments.project_name
WHERE p.is_active = TRUE;

-- 系统统计视图
DROP VIEW IF EXISTS system_stats;
CREATE VIEW system_stats AS
SELECT 'users' as metric, COUNT(*) as value FROM users
UNION ALL
SELECT 'projects' as metric, COUNT(*) as value FROM projects WHERE is_active = TRUE
UNION ALL
SELECT 'total_changes' as metric, COUNT(*) as value FROM changes
UNION ALL
SELECT 'open_changes' as metric, COUNT(*) as value FROM changes WHERE status = 'OPEN'
UNION ALL
SELECT 'total_reviews' as metric, COUNT(*) as value FROM reviews WHERE is_active = TRUE
UNION ALL
SELECT 'pending_reviews' as metric, COUNT(*) as value FROM reviews WHERE status = 'PENDING' AND is_active = TRUE
UNION ALL
SELECT 'total_comments' as metric, COUNT(*) as value FROM comments
UNION ALL
SELECT 'unresolved_comments' as metric, COUNT(*) as value FROM comments WHERE is_resolved = FALSE;

-- ========================================
-- 性能优化索引（如果表存在的话）
-- ========================================

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_reviews_change_reviewer ON reviews(change_id, reviewer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status_active ON reviews(status, is_active);
CREATE INDEX IF NOT EXISTS idx_comments_change_type ON comments(change_id, type);
CREATE INDEX IF NOT EXISTS idx_comments_file_line ON comments(file_path, line_number);
CREATE INDEX IF NOT EXISTS idx_changes_project_status ON changes(project_name, status);
CREATE INDEX IF NOT EXISTS idx_changes_submitter_status ON changes(submitter_id, status);

-- 时间相关索引
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews(created_at);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);
CREATE INDEX IF NOT EXISTS idx_changes_created_at ON changes(created_at);

-- 用于统计的索引
CREATE INDEX IF NOT EXISTS idx_comments_resolved ON comments(is_resolved);