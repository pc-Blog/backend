-- ============================================
-- Blog Database Schema (PostgreSQL)
-- Table prefix: t_
-- Logic delete: deleted (0=active, 1=deleted)
-- ============================================

-- 1. Admin User
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

-- 2. Unified Category (ARTICLE / PROJECT)
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    type        VARCHAR(16)     NOT NULL,  -- 'ARTICLE' or 'PROJECT'
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);

-- 3. Article Tag
CREATE TABLE IF NOT EXISTS t_tag (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(32)     NOT NULL UNIQUE,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);

-- 4. Blog Article
CREATE TABLE IF NOT EXISTS t_article (
    id           BIGSERIAL      PRIMARY KEY,
    title        VARCHAR(256)   NOT NULL,
    summary      VARCHAR(512),
    content      TEXT           NOT NULL,
    cover_image  VARCHAR(512),
    category_id  BIGINT         REFERENCES t_category(id),
    is_pinned    INTEGER        NOT NULL DEFAULT 0,  -- 0=normal, 1=pinned
    is_published INTEGER        NOT NULL DEFAULT 1,  -- 0=draft, 1=published
    view_count   BIGINT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMP,                          -- article date (can be backdated)
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_article_category ON t_article(category_id);
CREATE INDEX IF NOT EXISTS idx_article_published ON t_article(is_published, deleted);

-- 5. Article-Tag Junction (M:N)
CREATE TABLE IF NOT EXISTS t_article_tag (
    id          BIGSERIAL       PRIMARY KEY,
    article_id  BIGINT          NOT NULL REFERENCES t_article(id) ON DELETE CASCADE,
    tag_id      BIGINT          NOT NULL REFERENCES t_tag(id) ON DELETE CASCADE,
    UNIQUE(article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_article_tag_article ON t_article_tag(article_id);
CREATE INDEX IF NOT EXISTS idx_article_tag_tag ON t_article_tag(tag_id);

-- 6. Portfolio Project
CREATE TABLE IF NOT EXISTS t_project (
    id           BIGSERIAL      PRIMARY KEY,
    name         VARCHAR(128)   NOT NULL,
    summary      VARCHAR(512),
    content      TEXT,
    cover_image  VARCHAR(512),
    category_id  BIGINT         REFERENCES t_category(id),
    tech_stack   VARCHAR(512),                 -- JSON array: ["Spring","React"]
    github_url   VARCHAR(512),
    demo_url     VARCHAR(512),
    sort_order   INTEGER        NOT NULL DEFAULT 0,
    is_published INTEGER        NOT NULL DEFAULT 1,
    deleted      INTEGER        NOT NULL DEFAULT 0,
    create_time  TIMESTAMP      NOT NULL DEFAULT NOW(),
    update_time  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_category ON t_project(category_id);

-- 7. Learning Timeline
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

-- 8. Skill Proficiency
CREATE TABLE IF NOT EXISTS t_skill (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(64)     NOT NULL,
    category    VARCHAR(64),                  -- grouping: Backend/Frontend/DevOps
    proficiency INTEGER         NOT NULL DEFAULT 0,  -- 0-100
    sort_order  INTEGER         NOT NULL DEFAULT 0,
    deleted     INTEGER         NOT NULL DEFAULT 0,
    create_time TIMESTAMP       NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);

-- 9. About Me (key-value store for extensible personal info)
CREATE TABLE IF NOT EXISTS t_about (
    id          BIGSERIAL    PRIMARY KEY,
    item_key    VARCHAR(64)  NOT NULL,
    item_value  TEXT,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    deleted     INTEGER      NOT NULL DEFAULT 0,
    create_time TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP
);

-- 10. Article Comment (guest, no moderation)
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

-- 11. Uploaded Media / Files (MinIO)
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
