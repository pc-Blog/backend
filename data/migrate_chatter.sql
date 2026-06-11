-- 说说表
CREATE TABLE t_chatter (
  id           bigserial    PRIMARY KEY,
  content      text         NOT NULL,
  images       jsonb        DEFAULT '[]',
  mood         varchar(50)  DEFAULT '',
  is_published smallint     DEFAULT 1,
  deleted      smallint     DEFAULT 0,
  create_time  timestamp    DEFAULT CURRENT_TIMESTAMP,
  update_time  timestamp    DEFAULT CURRENT_TIMESTAMP
);
