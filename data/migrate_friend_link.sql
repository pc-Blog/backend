-- ============================================
-- Friend Link table
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

COMMENT ON TABLE t_friend_link IS '友情链接';
COMMENT ON COLUMN t_friend_link.name IS '站点名称';
COMMENT ON COLUMN t_friend_link.url IS '站点链接';
COMMENT ON COLUMN t_friend_link.description IS '站点描述';
COMMENT ON COLUMN t_friend_link.avatar IS '头像URL';
COMMENT ON COLUMN t_friend_link.theme_color IS '主题色';
COMMENT ON COLUMN t_friend_link.sort_order IS '排序';
COMMENT ON COLUMN t_friend_link.is_published IS '发布状态 1=显示 0=隐藏';
