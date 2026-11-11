-- ============================================================================
-- 播放记录统计（每用户每首歌的播放次数 + 每首歌的总播放次数）
-- 1) music_record 地基：create_time 默认值 + 索引（解决「数据量大就慢」）
-- 2) 两张统计表：user_music_play_stat / music_play_stat
-- 3) 存量数据回填（幂等，可重复执行）
-- 两个项目（Spring Boot / FastAPI）共用同一套表结构与 SQL
-- ============================================================================

-- 1) 原始表地基 ---------------------------------------------------------------
-- 原来是 DEFAULT NULL，插入时没写 create_time 的记录会变成 NULL，导致「最近播放/时间范围统计」不准
ALTER TABLE music_record MODIFY COLUMN create_time datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间';
-- 按需创建索引（已存在会报 duplicate key name，可先 SELECT 检查或忽略该错误）
CREATE INDEX idx_user_music_time ON music_record (user_id, music_id, create_time);
CREATE INDEX idx_music_time      ON music_record (music_id, create_time);
CREATE INDEX idx_create_time     ON music_record (create_time);

-- 2) 统计表 -------------------------------------------------------------------
-- 用户播放记录统计表：主键 (user_id, music_id)，播放一次 +1
CREATE TABLE IF NOT EXISTS `user_music_play_stat` (
  `user_id` varchar(64) NOT NULL COMMENT '用户id',
  `music_id` int NOT NULL COMMENT '音乐id',
  `play_count` int unsigned NOT NULL DEFAULT 0 COMMENT '该用户播放该歌曲的总次数',
  `first_play_time` datetime DEFAULT NULL COMMENT '首次播放时间',
  `last_play_time` datetime DEFAULT NULL COMMENT '最近播放时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`,`music_id`),
  KEY `idx_user_last` (`user_id`,`last_play_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户播放记录统计表（每用户每首歌的播放次数，music_record 的汇总）';

-- 歌曲播放记录统计表：主键 music_id，播放一次 +1（用于搜索排名/排行榜）
CREATE TABLE IF NOT EXISTS `music_play_stat` (
  `music_id` int NOT NULL COMMENT '音乐id',
  `play_count` bigint unsigned NOT NULL DEFAULT 0 COMMENT '该歌曲的播放总次数',
  `first_play_time` datetime DEFAULT NULL COMMENT '首次播放时间',
  `last_play_time` datetime DEFAULT NULL COMMENT '最近播放时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`music_id`),
  KEY `idx_play_count` (`play_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='歌曲播放记录统计表（每首歌的播放总次数，用于搜索/排行榜）';

-- 3) 存量数据回填（幂等：每次都按原始表重算并覆盖） ---------------------------
INSERT INTO user_music_play_stat (user_id, music_id, play_count, first_play_time, last_play_time, create_time, update_time)
SELECT user_id, music_id, COUNT(*), MIN(create_time), MAX(create_time), NOW(), NOW()
FROM music_record
WHERE user_id IS NOT NULL AND user_id <> '' AND music_id IS NOT NULL AND music_id > 0
GROUP BY user_id, music_id
ON DUPLICATE KEY UPDATE
  play_count = VALUES(play_count),
  first_play_time = VALUES(first_play_time),
  last_play_time = VALUES(last_play_time),
  update_time = NOW();

INSERT INTO music_play_stat (music_id, play_count, first_play_time, last_play_time, create_time, update_time)
SELECT music_id, COUNT(*), MIN(create_time), MAX(create_time), NOW(), NOW()
FROM music_record
WHERE music_id IS NOT NULL AND music_id > 0
GROUP BY music_id
ON DUPLICATE KEY UPDATE
  play_count = VALUES(play_count),
  first_play_time = VALUES(first_play_time),
  last_play_time = VALUES(last_play_time),
  update_time = NOW();

-- 对账 SQL（两边应当相等）：
-- SELECT (SELECT COUNT(*) FROM music_record WHERE user_id IS NOT NULL AND user_id<>'' AND music_id>0) AS raw,
--        (SELECT SUM(play_count) FROM user_music_play_stat) AS user_sum,
--        (SELECT SUM(play_count) FROM music_play_stat) AS music_sum;
