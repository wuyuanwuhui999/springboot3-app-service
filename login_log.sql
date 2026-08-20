-- 登录日志表（不含 user_account：user_account 可能变化，user_id 永远不变）
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
    `user_id` VARCHAR(50) DEFAULT NULL COMMENT '用户ID',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `login_type` VARCHAR(50) DEFAULT NULL COMMENT '登录类型：login/getUserData',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';
