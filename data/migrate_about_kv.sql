-- ============================================
-- t_about 从宽表迁移到 Key-Value 结构
-- 执行前请确认已备份数据
-- ============================================

-- 1. 备份旧数据（宽表结构）
DROP TABLE IF EXISTS t_about_backup;
CREATE TABLE t_about_backup AS SELECT * FROM t_about;

-- 2. 记录旧数据用于迁移
DO $$
DECLARE
    old_content      TEXT;
    old_email        VARCHAR(128);
    old_github       VARCHAR(512);
    old_social       TEXT;
BEGIN
    SELECT content, contact_email, github_url, social_links
    INTO old_content, old_email, old_github, old_social
    FROM t_about_backup LIMIT 1;

    -- 3. 删除旧表，创建新表
    DROP TABLE IF EXISTS t_about CASCADE;
    CREATE TABLE t_about (
        id          BIGSERIAL    PRIMARY KEY,
        item_key    VARCHAR(64)  NOT NULL,
        item_value  TEXT,
        sort_order  INTEGER      NOT NULL DEFAULT 0,
        deleted     INTEGER      NOT NULL DEFAULT 0,
        create_time TIMESTAMP    NOT NULL DEFAULT NOW(),
        update_time TIMESTAMP
    );

    -- 4. 迁移旧数据到 K-V 格式
    IF old_content IS NOT NULL AND old_content != '' THEN
        INSERT INTO t_about (item_key, item_value, sort_order) VALUES ('content', old_content, 1);
    END IF;
    IF old_email IS NOT NULL AND old_email != '' THEN
        INSERT INTO t_about (item_key, item_value, sort_order) VALUES ('contactEmail', old_email, 2);
    END IF;
    IF old_github IS NOT NULL AND old_github != '' THEN
        INSERT INTO t_about (item_key, item_value, sort_order) VALUES ('githubUrl', old_github, 3);
    END IF;
    IF old_social IS NOT NULL AND old_social != '' THEN
        INSERT INTO t_about (item_key, item_value, sort_order) VALUES ('socialLinks', old_social, 4);
    END IF;
END $$;

-- 5. 验证迁移结果
SELECT item_key, LEFT(item_value, 40) AS value_preview, sort_order FROM t_about ORDER BY sort_order;
