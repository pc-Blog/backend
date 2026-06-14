-- ============================================
-- Blog Database Schema (PostgreSQL)
-- 统一建表脚本 — 开箱即用
-- ============================================
-- 使用方式：
--   psql -U your_user -d your_db -f init.sql
-- 或直接在 SQL 工具中执行
-- ============================================

-- 启用 UUID 扩展（如需）
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. 管理员用户
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(64)     NOT NULL UNIQUE,
    password    VARCHAR(128)    NOT NULL,
    nickname    VARCHAR(64),
    avatar      VARCHAR(512),
    email       VARCHAR(128),
    github_id   VARCHAR(32),
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_user              IS '管理员用户';
COMMENT ON COLUMN t_user.github_id    IS 'GitHub OAuth ID（用于第三方登录）';

-- ============================================
-- 2. 统一分类（ARTICLE / PROJECT）
-- ============================================
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    type        VARCHAR(16)     NOT NULL,  -- 'ARTICLE' 或 'PROJECT'
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_category           IS '文章/项目分类';
COMMENT ON COLUMN t_category.type      IS '分类类型: ARTICLE | PROJECT';

-- ============================================
-- 3. 文章标签
-- ============================================
CREATE TABLE IF NOT EXISTS t_tag (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(32)     NOT NULL UNIQUE,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_tag                IS '文章标签';

-- ============================================
-- 4. 博客文章
-- ============================================
CREATE TABLE IF NOT EXISTS t_article (
    id           BIGSERIAL      PRIMARY KEY,
    title        VARCHAR(256)   NOT NULL,
    summary      VARCHAR(512),
    content      TEXT           NOT NULL,
    cover_image  VARCHAR(512),
    category_id  BIGINT         REFERENCES t_category(id),
    is_pinned    INTEGER        NOT NULL DEFAULT 0,  -- 0=普通 1=置顶
    is_published INTEGER        NOT NULL DEFAULT 1,  -- 0=草稿 1=已发布
    view_count   BIGINT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMP,                          -- 文章创作日期（可回溯）
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_article_category  ON t_article(category_id);
CREATE INDEX IF NOT EXISTS idx_article_published ON t_article(is_published, deleted);
COMMENT ON TABLE  t_article            IS '博客文章';

-- ============================================
-- 5. 文章-标签关联（多对多）
-- ============================================
CREATE TABLE IF NOT EXISTS t_article_tag (
    id          BIGSERIAL       PRIMARY KEY,
    article_id  BIGINT          NOT NULL REFERENCES t_article(id) ON DELETE CASCADE,
    tag_id      BIGINT          NOT NULL REFERENCES t_tag(id) ON DELETE CASCADE,
    UNIQUE(article_id, tag_id)
);
CREATE INDEX IF NOT EXISTS idx_article_tag_article ON t_article_tag(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tag_tag     ON t_article_tag(tag_id);

-- ============================================
-- 6. 项目作品
-- ============================================
CREATE TABLE IF NOT EXISTS t_project (
    id           BIGSERIAL      PRIMARY KEY,
    name         VARCHAR(128)   NOT NULL,
    summary      VARCHAR(512),
    content      TEXT,
    cover_image  VARCHAR(512),
    category_id  BIGINT         REFERENCES t_category(id),
    tech_stack   VARCHAR(512),                 -- JSON 数组: ["Spring","React"]
    github_url   VARCHAR(512),
    demo_url     VARCHAR(512),
    sort_order   INTEGER        NOT NULL DEFAULT 0,
    is_published INTEGER        NOT NULL DEFAULT 1,
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_project_category ON t_project(category_id);
COMMENT ON TABLE  t_project            IS '项目作品集';

-- ============================================
-- 7. 学习时间线
-- ============================================
CREATE TABLE IF NOT EXISTS t_timeline (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(128)    NOT NULL,
    description TEXT,
    event_date  DATE            NOT NULL,
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_timeline           IS '学习历程/里程碑';

-- ============================================
-- 8. 技能熟练度
-- ============================================
CREATE TABLE IF NOT EXISTS t_skill (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    category    VARCHAR(64),                  -- 分组: Backend/Frontend/DevOps
    proficiency INTEGER         NOT NULL DEFAULT 0,  -- 0-100
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_skill              IS '技能熟练度';

-- ============================================
-- 9. 关于页（Key-Value 结构，可扩展）
-- ============================================
CREATE TABLE IF NOT EXISTS t_about (
    id          BIGSERIAL    PRIMARY KEY,
    item_key    VARCHAR(64)  NOT NULL,
    item_value  TEXT,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    deleted     INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_about              IS '关于页配置（K-V 结构，可自由扩展）';

-- ============================================
-- 10. 访客评论
-- ============================================
CREATE TABLE IF NOT EXISTS t_comment (
    id           BIGSERIAL      PRIMARY KEY,
    article_id   BIGINT         NOT NULL REFERENCES t_article(id) ON DELETE CASCADE,
    author_name  VARCHAR(64)    NOT NULL,
    author_email VARCHAR(128),
    content      TEXT           NOT NULL,
    user_id      BIGINT,
    user_agent   VARCHAR(512),
    ip_address   VARCHAR(64),
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_comment_article ON t_comment(article_id, deleted);
COMMENT ON TABLE  t_comment            IS '文章评论';

-- ============================================
-- 11. 上传媒体文件
-- ============================================
CREATE TABLE IF NOT EXISTS t_media (
    id                BIGSERIAL   PRIMARY KEY,
    filename          VARCHAR(256) NOT NULL,
    original_filename VARCHAR(256),
    file_path         VARCHAR(512),
    file_url          VARCHAR(512),
    file_size         BIGINT,
    mime_type         VARCHAR(128),
    relation_type     VARCHAR(32),
    deleted           INTEGER      NOT NULL DEFAULT 0,
    create_time       TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_time       TIMESTAMP
);
COMMENT ON TABLE  t_media              IS '媒体文件记录';

-- ============================================
-- 12. 说说/动态
-- ============================================
CREATE TABLE IF NOT EXISTS t_chatter (
    id           BIGSERIAL    PRIMARY KEY,
    content      TEXT         NOT NULL,
    images       JSONB        DEFAULT '[]',
    mood         VARCHAR(50)  DEFAULT '',
    is_published SMALLINT     DEFAULT 1,
    deleted      SMALLINT     DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  t_chatter            IS '说说/日常动态';
COMMENT ON COLUMN t_chatter.images     IS '图片列表（JSON 数组）';
COMMENT ON COLUMN t_chatter.mood       IS '心情标签';

-- ============================================
-- 13. 友情链接
-- ============================================
CREATE TABLE IF NOT EXISTS t_friend_link (
    id           BIGSERIAL       PRIMARY KEY,
    name         VARCHAR(100)    NOT NULL,
    url          VARCHAR(500)    NOT NULL,
    description  VARCHAR(255),
    avatar       VARCHAR(500),
    theme_color  VARCHAR(50),
    sort_order   INTEGER         NOT NULL DEFAULT 0,
    is_published INTEGER         NOT NULL DEFAULT 1,
    deleted      INTEGER         NOT NULL DEFAULT 0,
    create_time  TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
COMMENT ON TABLE  t_friend_link        IS '友情链接';
COMMENT ON COLUMN t_friend_link.theme_color IS '站点主题色（用于卡片展示）';
