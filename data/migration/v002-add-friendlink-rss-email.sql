-- v002: 友链表新增 rss / email 字段
-- 对应前端 feaet(tools): 友链添加 RSS 和邮箱字段

ALTER TABLE t_friend_link
  ADD COLUMN IF NOT EXISTS rss   VARCHAR(500);

ALTER TABLE t_friend_link
  ADD COLUMN IF NOT EXISTS email VARCHAR(255);

COMMENT ON COLUMN t_friend_link.rss   IS 'RSS订阅链接';
COMMENT ON COLUMN t_friend_link.email IS '联系邮箱';
