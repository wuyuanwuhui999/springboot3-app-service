-- chat_doc 文档权限字段迁移
-- 新增 permission 字段：private-私密，tenant-租户内公开，company-公司内公开
-- 旧数据默认设置为 private（私密）
ALTER TABLE chat_doc
    ADD COLUMN permission varchar(20) NOT NULL DEFAULT 'private'
    COMMENT '文档权限：private-私密，tenant-租户内公开，company-公司内公开';
