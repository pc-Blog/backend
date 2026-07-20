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
    id                   BIGSERIAL       PRIMARY KEY,
    username             VARCHAR(64)     NOT NULL UNIQUE,
    password             VARCHAR(128)    NOT NULL,
    nickname             VARCHAR(64),
    avatar               VARCHAR(512),
    email                VARCHAR(128),
    github_id            VARCHAR(32),
    github_token         VARCHAR(512),
    github_refresh_token VARCHAR(512),
    github_token_expires_at VARCHAR(32),
    create_time          TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time          TIMESTAMP
);
COMMENT ON TABLE  t_user                     IS '管理员用户';
COMMENT ON COLUMN t_user.id                  IS '主键ID';
COMMENT ON COLUMN t_user.username            IS '用户名，唯一';
COMMENT ON COLUMN t_user.password            IS '密码（BCrypt 加密）';
COMMENT ON COLUMN t_user.nickname            IS '昵称';
COMMENT ON COLUMN t_user.avatar              IS '头像URL';
COMMENT ON COLUMN t_user.email               IS '邮箱';
COMMENT ON COLUMN t_user.github_id           IS 'GitHub OAuth ID';
COMMENT ON COLUMN t_user.github_token        IS 'GitHub OAuth access token';
COMMENT ON COLUMN t_user.github_refresh_token IS 'GitHub OAuth refresh token';
COMMENT ON COLUMN t_user.github_token_expires_at IS 'GitHub token 过期时间';
COMMENT ON COLUMN t_user.create_time         IS '创建时间';
COMMENT ON COLUMN t_user.update_time         IS '更新时间';

-- ============================================
-- 2. 统一分类（ARTICLE / PROJECT）
-- ============================================
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    type        VARCHAR(16)     NOT NULL,
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_category_type ON t_category(type, deleted);
COMMENT ON TABLE  t_category           IS '文章/项目分类';
COMMENT ON COLUMN t_category.id        IS '主键ID';
COMMENT ON COLUMN t_category.name      IS '分类名称';
COMMENT ON COLUMN t_category.type      IS '分类类型：ARTICLE | PROJECT';
COMMENT ON COLUMN t_category.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_category.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_category.create_time IS '创建时间';
COMMENT ON COLUMN t_category.update_time IS '更新时间';

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
COMMENT ON TABLE  t_tag           IS '文章标签';
COMMENT ON COLUMN t_tag.id        IS '主键ID';
COMMENT ON COLUMN t_tag.name      IS '标签名称，唯一';
COMMENT ON COLUMN t_tag.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_tag.create_time IS '创建时间';
COMMENT ON COLUMN t_tag.update_time IS '更新时间';

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
    is_pinned    INTEGER        NOT NULL DEFAULT 0,
    is_published INTEGER        NOT NULL DEFAULT 1,
    view_count   BIGINT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMP,
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_article_category  ON t_article(category_id);
CREATE INDEX IF NOT EXISTS idx_article_published ON t_article(is_published, deleted);
COMMENT ON TABLE  t_article            IS '博客文章';
COMMENT ON COLUMN t_article.id         IS '主键ID';
COMMENT ON COLUMN t_article.title      IS '文章标题';
COMMENT ON COLUMN t_article.summary    IS '文章摘要';
COMMENT ON COLUMN t_article.content    IS '文章内容（Markdown）';
COMMENT ON COLUMN t_article.cover_image IS '封面图URL';
COMMENT ON COLUMN t_article.category_id IS '所属分类ID';
COMMENT ON COLUMN t_article.is_pinned  IS '是否置顶：0=普通 1=置顶';
COMMENT ON COLUMN t_article.is_published IS '发布状态：0=草稿 1=已发布';
COMMENT ON COLUMN t_article.view_count IS '浏览数';
COMMENT ON COLUMN t_article.created_at IS '文章创作日期（可回溯）';
COMMENT ON COLUMN t_article.deleted    IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_article.create_time IS '创建时间';
COMMENT ON COLUMN t_article.update_time IS '更新时间';

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
COMMENT ON TABLE  t_article_tag           IS '文章-标签关联表（多对多）';
COMMENT ON COLUMN t_article_tag.id        IS '主键ID';
COMMENT ON COLUMN t_article_tag.article_id IS '文章ID';
COMMENT ON COLUMN t_article_tag.tag_id    IS '标签ID';

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
COMMENT ON COLUMN t_project.id         IS '主键ID';
COMMENT ON COLUMN t_project.name       IS '项目名称';
COMMENT ON COLUMN t_project.summary    IS '项目简介';
COMMENT ON COLUMN t_project.content    IS '项目详情（Markdown）';
COMMENT ON COLUMN t_project.cover_image IS '封面图URL';
COMMENT ON COLUMN t_project.category_id IS '所属分类ID';
COMMENT ON COLUMN t_project.github_url IS 'GitHub 仓库地址';
COMMENT ON COLUMN t_project.demo_url   IS '在线演示地址';
COMMENT ON COLUMN t_project.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_project.is_published IS '发布状态：0=草稿 1=已发布';
COMMENT ON COLUMN t_project.deleted    IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_project.create_time IS '创建时间';
COMMENT ON COLUMN t_project.update_time IS '更新时间';

-- ============================================
-- 7. 技术栈
-- ============================================
CREATE TABLE IF NOT EXISTS t_technology (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_technology           IS '技术栈';
COMMENT ON COLUMN t_technology.id        IS '主键ID';
COMMENT ON COLUMN t_technology.name      IS '技术名称';
COMMENT ON COLUMN t_technology.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_technology.create_time IS '创建时间';
COMMENT ON COLUMN t_technology.update_time IS '更新时间';

-- ============================================
-- 8. 项目-技术关联（多对多）
-- ============================================
CREATE TABLE IF NOT EXISTS t_project_tech (
    id          BIGSERIAL       PRIMARY KEY,
    project_id  BIGINT          NOT NULL REFERENCES t_project(id),
    tech_id     BIGINT          NOT NULL REFERENCES t_technology(id),
    UNIQUE(project_id, tech_id)
);
COMMENT ON TABLE  t_project_tech           IS '项目-技术关联表（多对多）';
COMMENT ON COLUMN t_project_tech.id        IS '主键ID';
COMMENT ON COLUMN t_project_tech.project_id IS '项目ID';
COMMENT ON COLUMN t_project_tech.tech_id   IS '技术ID';

-- ============================================
-- 9. 学习时间线
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
COMMENT ON COLUMN t_timeline.id        IS '主键ID';
COMMENT ON COLUMN t_timeline.title     IS '里程碑标题';
COMMENT ON COLUMN t_timeline.description IS '详细描述';
COMMENT ON COLUMN t_timeline.event_date IS '发生日期';
COMMENT ON COLUMN t_timeline.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_timeline.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_timeline.create_time IS '创建时间';
COMMENT ON COLUMN t_timeline.update_time IS '更新时间';

-- ============================================
-- 10. 技能熟练度
-- ============================================
CREATE TABLE IF NOT EXISTS t_skill (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    category    VARCHAR(64),
    proficiency INTEGER         NOT NULL DEFAULT 0,
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
COMMENT ON TABLE  t_skill           IS '技能熟练度';
COMMENT ON COLUMN t_skill.id        IS '主键ID';
COMMENT ON COLUMN t_skill.name      IS '技能名称';
COMMENT ON COLUMN t_skill.category  IS '分组：Backend/Frontend/DevOps 等';
COMMENT ON COLUMN t_skill.proficiency IS '熟练度（0-100）';
COMMENT ON COLUMN t_skill.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_skill.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_skill.create_time IS '创建时间';
COMMENT ON COLUMN t_skill.update_time IS '更新时间';

-- ============================================
-- 11. 关于页（Key-Value 结构）
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
COMMENT ON COLUMN t_about.id           IS '主键ID';
COMMENT ON COLUMN t_about.item_key     IS '配置键';
COMMENT ON COLUMN t_about.item_value   IS '配置值';
COMMENT ON COLUMN t_about.sort_order   IS '排序值，越小越靠前';
COMMENT ON COLUMN t_about.deleted      IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_about.create_time  IS '创建时间';
COMMENT ON COLUMN t_about.update_time  IS '更新时间';

-- ============================================
-- 12. 访客评论
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
COMMENT ON COLUMN t_comment.id         IS '主键ID';
COMMENT ON COLUMN t_comment.article_id IS '关联文章ID';
COMMENT ON COLUMN t_comment.author_name IS '评论者昵称';
COMMENT ON COLUMN t_comment.author_email IS '评论者邮箱';
COMMENT ON COLUMN t_comment.content    IS '评论内容';
COMMENT ON COLUMN t_comment.user_id    IS '登录用户ID（游客为NULL）';
COMMENT ON COLUMN t_comment.user_agent IS '用户浏览器UA';
COMMENT ON COLUMN t_comment.ip_address IS '评论者IP地址';
COMMENT ON COLUMN t_comment.deleted    IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_comment.create_time IS '创建时间';
COMMENT ON COLUMN t_comment.update_time IS '更新时间';

-- ============================================
-- 13. 上传媒体文件
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
COMMENT ON COLUMN t_media.id           IS '主键ID';
COMMENT ON COLUMN t_media.filename     IS '存储文件名';
COMMENT ON COLUMN t_media.original_filename IS '原始文件名';
COMMENT ON COLUMN t_media.file_path    IS '存储路径';
COMMENT ON COLUMN t_media.file_url     IS '访问URL';
COMMENT ON COLUMN t_media.file_size    IS '文件大小（字节）';
COMMENT ON COLUMN t_media.mime_type    IS 'MIME 类型';
COMMENT ON COLUMN t_media.relation_type IS '关联业务类型';
COMMENT ON COLUMN t_media.deleted      IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_media.create_time  IS '创建时间';
COMMENT ON COLUMN t_media.update_time  IS '更新时间';

-- ============================================
-- 14. 相册
-- ============================================
CREATE TABLE IF NOT EXISTS t_album (
    id           BIGSERIAL       PRIMARY KEY,
    title        VARCHAR(255)    NOT NULL,
    description  TEXT,
    sort_order   INTEGER         DEFAULT 0,
    is_published SMALLINT        DEFAULT 1,
    deleted      SMALLINT        DEFAULT 0,
    create_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  t_album               IS '相册';
COMMENT ON COLUMN t_album.id            IS '主键ID';
COMMENT ON COLUMN t_album.title         IS '相册标题';
COMMENT ON COLUMN t_album.description   IS '相册描述';
COMMENT ON COLUMN t_album.sort_order    IS '排序值，越小越靠前';
COMMENT ON COLUMN t_album.is_published  IS '发布状态：0=隐藏 1=发布';
COMMENT ON COLUMN t_album.deleted       IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_album.create_time   IS '创建时间';
COMMENT ON COLUMN t_album.update_time   IS '更新时间';

-- ============================================
-- 15. 相片（一对多）
-- ============================================
CREATE TABLE IF NOT EXISTS t_photo (
    id          BIGSERIAL       PRIMARY KEY,
    album_id    BIGINT          NOT NULL REFERENCES t_album(id),
    url         VARCHAR(500)    NOT NULL,
    sort_order  INTEGER         DEFAULT 0,
    deleted     SMALLINT        DEFAULT 0,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_photo_album ON t_photo(album_id);
COMMENT ON TABLE  t_photo               IS '相片（一对多）';
COMMENT ON COLUMN t_photo.id            IS '主键ID';
COMMENT ON COLUMN t_photo.album_id      IS '所属相册ID';
COMMENT ON COLUMN t_photo.url           IS '图片URL';
COMMENT ON COLUMN t_photo.sort_order    IS '排序值，越小越靠前';
COMMENT ON COLUMN t_photo.deleted       IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_photo.create_time   IS '创建时间';
COMMENT ON COLUMN t_photo.update_time   IS '更新时间';

-- ============================================
-- 16. 说说/动态
-- ============================================
CREATE TABLE IF NOT EXISTS t_chatter (
    id           BIGSERIAL    PRIMARY KEY,
    content      TEXT         NOT NULL,
    mood         VARCHAR(50)  DEFAULT '',
    is_published SMALLINT     DEFAULT 1,
    deleted      SMALLINT     DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  t_chatter            IS '说说/日常动态';
COMMENT ON COLUMN t_chatter.id         IS '主键ID';
COMMENT ON COLUMN t_chatter.content    IS '动态内容';
COMMENT ON COLUMN t_chatter.mood       IS '心情标签';
COMMENT ON COLUMN t_chatter.is_published IS '发布状态：0=隐藏 1=发布';
COMMENT ON COLUMN t_chatter.deleted    IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_chatter.create_time IS '创建时间';
COMMENT ON COLUMN t_chatter.update_time IS '更新时间';

-- ============================================
-- 17. 说说图片（一对多）
-- ============================================
CREATE TABLE IF NOT EXISTS t_chatter_image (
    id          BIGSERIAL       PRIMARY KEY,
    chatter_id  BIGINT          NOT NULL REFERENCES t_chatter(id),
    url         VARCHAR(500)    NOT NULL,
    sort_order  INTEGER         DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_chatter_image_chatter ON t_chatter_image(chatter_id);
COMMENT ON TABLE  t_chatter_image          IS '说说图片（一对多）';
COMMENT ON COLUMN t_chatter_image.id       IS '主键ID';
COMMENT ON COLUMN t_chatter_image.chatter_id IS '关联说说ID';
COMMENT ON COLUMN t_chatter_image.url     IS '图片URL';
COMMENT ON COLUMN t_chatter_image.sort_order IS '排序值，越小越靠前';

-- ============================================
-- 18. 友情链接
-- ============================================
CREATE TABLE IF NOT EXISTS t_friend_link (
    id           BIGSERIAL       PRIMARY KEY,
    name         VARCHAR(100)    NOT NULL,
    url          VARCHAR(500)    NOT NULL,
    description  VARCHAR(255),
    avatar       VARCHAR(500),
    rss          VARCHAR(500),
    email        VARCHAR(255),
    theme_color  VARCHAR(50),
    sort_order   INTEGER         NOT NULL DEFAULT 0,
    is_published INTEGER         NOT NULL DEFAULT 1,
    deleted      INTEGER         NOT NULL DEFAULT 0,
    create_time  TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
COMMENT ON TABLE  t_friend_link        IS '友情链接';
COMMENT ON COLUMN t_friend_link.id     IS '主键ID';
COMMENT ON COLUMN t_friend_link.name   IS '站点名称';
COMMENT ON COLUMN t_friend_link.url    IS '站点链接';
COMMENT ON COLUMN t_friend_link.description IS '站点描述';
COMMENT ON COLUMN t_friend_link.avatar IS '站点头像URL';
COMMENT ON COLUMN t_friend_link.rss    IS 'RSS订阅链接';
COMMENT ON COLUMN t_friend_link.email  IS '联系邮箱';
COMMENT ON COLUMN t_friend_link.theme_color IS '站点主题色（用于卡片展示）';
COMMENT ON COLUMN t_friend_link.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_friend_link.is_published IS '发布状态：0=隐藏 1=发布';
COMMENT ON COLUMN t_friend_link.deleted IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_friend_link.create_time IS '创建时间';
COMMENT ON COLUMN t_friend_link.update_time IS '更新时间';

-- ============================================
-- 19. 邮件归档（从 Worker D1 同步）
-- ============================================
CREATE TABLE IF NOT EXISTS t_email (
    id          BIGSERIAL    PRIMARY KEY,
    message_id  VARCHAR(255) NOT NULL,
    from_addr   VARCHAR(255) NOT NULL,
    to_addr     VARCHAR(255) NOT NULL,
    forward_to  VARCHAR(255) NOT NULL DEFAULT '',
    subject     TEXT         NOT NULL DEFAULT '',
    text_body   TEXT         NOT NULL DEFAULT '',
    html_body   TEXT         NOT NULL DEFAULT '',
    headers     TEXT         NOT NULL DEFAULT '{}',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_t_email_message_id ON t_email(message_id);
COMMENT ON TABLE  t_email IS '邮件归档（从 Worker D1 同步）';
COMMENT ON COLUMN t_email.id IS '主键ID';
COMMENT ON COLUMN t_email.message_id IS '邮件消息ID';
COMMENT ON COLUMN t_email.from_addr IS '发件人地址';
COMMENT ON COLUMN t_email.to_addr IS '收件人地址';
COMMENT ON COLUMN t_email.forward_to IS '转发目标地址';
COMMENT ON COLUMN t_email.subject IS '邮件主题';
COMMENT ON COLUMN t_email.text_body IS '纯文本正文';
COMMENT ON COLUMN t_email.html_body IS 'HTML 正文';
COMMENT ON COLUMN t_email.headers IS '邮件头（JSON）';
COMMENT ON COLUMN t_email.created_at IS '接收时间';

-- ============================================
-- 20. 邮件订阅者（从 Worker D1 同步）
-- ============================================
CREATE TABLE IF NOT EXISTS t_subscriber (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    group_name  VARCHAR(64)  NOT NULL DEFAULT 'article',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(email, group_name)
);
CREATE INDEX IF NOT EXISTS idx_t_subscriber_email ON t_subscriber(email);
COMMENT ON TABLE  t_subscriber IS '邮件订阅者（从 Worker D1 同步）';
COMMENT ON COLUMN t_subscriber.id IS '主键ID';
COMMENT ON COLUMN t_subscriber.email IS '订阅者邮箱';
COMMENT ON COLUMN t_subscriber.group_name IS '订阅分组（article/hot）';
COMMENT ON COLUMN t_subscriber.created_at IS '订阅时间';

-- ============================================
-- 21. 评论 Emoji 反应（从 Worker D1 同步）
-- ============================================
CREATE TABLE IF NOT EXISTS t_comment_reaction (
    id          BIGSERIAL    PRIMARY KEY,
    subject_id  VARCHAR(128) NOT NULL,
    user_id     BIGINT       NOT NULL,
    reaction    VARCHAR(32)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(subject_id, user_id, reaction)
);
COMMENT ON TABLE  t_comment_reaction IS '评论 Emoji 反应（从 Worker D1 同步）';
COMMENT ON COLUMN t_comment_reaction.id IS '主键ID';
COMMENT ON COLUMN t_comment_reaction.subject_id IS '评论标识（讨论ID或评论ID）';
COMMENT ON COLUMN t_comment_reaction.user_id IS '用户ID';
COMMENT ON COLUMN t_comment_reaction.reaction IS 'Emoji 反应类型';
COMMENT ON COLUMN t_comment_reaction.created_at IS '创建时间';

-- ============================================
-- 22. 评论点赞（从 Worker D1 同步）
-- ============================================
CREATE TABLE IF NOT EXISTS t_comment_upvote (
    id          BIGSERIAL    PRIMARY KEY,
    subject_id  VARCHAR(128) NOT NULL,
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(subject_id, user_id)
);
COMMENT ON TABLE  t_comment_upvote IS '评论点赞（从 Worker D1 同步）';
COMMENT ON COLUMN t_comment_upvote.id IS '主键ID';
COMMENT ON COLUMN t_comment_upvote.subject_id IS '评论标识';
COMMENT ON COLUMN t_comment_upvote.user_id IS '用户ID';
COMMENT ON COLUMN t_comment_upvote.created_at IS '创建时间';

-- ============================================
-- 23. 推送记录（从 Worker D1 同步）
-- ============================================
CREATE TABLE IF NOT EXISTS t_push_log (
    id               BIGSERIAL    PRIMARY KEY,
    pushed_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    article_count    INTEGER      NOT NULL DEFAULT 0,
    subscriber_count INTEGER      NOT NULL DEFAULT 0,
    group_name       VARCHAR(64)  NOT NULL DEFAULT 'article',
    status           VARCHAR(32)  NOT NULL DEFAULT 'success',
    error_msg        TEXT,
    article_ids      TEXT         NOT NULL DEFAULT ''
);
CREATE INDEX IF NOT EXISTS idx_t_push_log_pushed_at ON t_push_log(pushed_at);
COMMENT ON TABLE  t_push_log IS '推送记录（从 Worker D1 同步）';
COMMENT ON COLUMN t_push_log.id IS '主键ID';
COMMENT ON COLUMN t_push_log.pushed_at IS '推送时间';
COMMENT ON COLUMN t_push_log.article_count IS '推送文章数';
COMMENT ON COLUMN t_push_log.subscriber_count IS '推送订阅者数';
COMMENT ON COLUMN t_push_log.group_name IS '推送分组';
COMMENT ON COLUMN t_push_log.status IS '推送状态（success/failed）';
COMMENT ON COLUMN t_push_log.error_msg IS '错误信息';
COMMENT ON COLUMN t_push_log.article_ids IS '推送文章ID列表';

-- ============================================
-- 24. 收藏分类
-- ============================================
CREATE TABLE IF NOT EXISTS t_bookmark_category (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    parent_id   BIGINT          REFERENCES t_bookmark_category(id),
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_bookmark_category_parent ON t_bookmark_category(parent_id);
COMMENT ON TABLE  t_bookmark_category           IS '收藏分类（支持二级分类）';
COMMENT ON COLUMN t_bookmark_category.id        IS '主键ID';
COMMENT ON COLUMN t_bookmark_category.name      IS '分类名称';
COMMENT ON COLUMN t_bookmark_category.parent_id IS '父分类ID，NULL=一级分类';
COMMENT ON COLUMN t_bookmark_category.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_bookmark_category.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_bookmark_category.create_time IS '创建时间';
COMMENT ON COLUMN t_bookmark_category.update_time IS '更新时间';

-- ============================================
-- 25. 收藏网站
-- ============================================
CREATE TABLE IF NOT EXISTS t_bookmark (
    id           BIGSERIAL       PRIMARY KEY,
    name         VARCHAR(100)    NOT NULL,
    url          VARCHAR(500)    NOT NULL,
    description  VARCHAR(255),
    icon         VARCHAR(50)     DEFAULT '🔗',
    category_id  BIGINT          REFERENCES t_bookmark_category(id),
    is_pin       INTEGER         NOT NULL DEFAULT 0,
    sort_order   INTEGER         NOT NULL DEFAULT 0,
    deleted      INTEGER         NOT NULL DEFAULT 0,
    create_time  TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_bookmark_pin       ON t_bookmark(is_pin, sort_order);
CREATE INDEX IF NOT EXISTS idx_bookmark_category  ON t_bookmark(category_id);
COMMENT ON TABLE  t_bookmark           IS '收藏网站';
COMMENT ON COLUMN t_bookmark.id        IS '主键ID';
COMMENT ON COLUMN t_bookmark.name      IS '网站名称';
COMMENT ON COLUMN t_bookmark.url       IS '网站链接';
COMMENT ON COLUMN t_bookmark.description IS '网站描述';
COMMENT ON COLUMN t_bookmark.icon      IS '显示图标/emoji';
COMMENT ON COLUMN t_bookmark.category_id IS '所属分类ID';
COMMENT ON COLUMN t_bookmark.is_pin    IS '是否 pin 到前端收藏夹展示：0=否 1=是';
COMMENT ON COLUMN t_bookmark.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN t_bookmark.deleted   IS '逻辑删除：0=正常 1=删除';
COMMENT ON COLUMN t_bookmark.create_time IS '创建时间';
COMMENT ON COLUMN t_bookmark.update_time IS '更新时间';
