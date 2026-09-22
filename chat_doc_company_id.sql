-- chat_doc 表新增 company_id 字段（permission=company 公司内公开时记录所属公司）
ALTER TABLE chat_doc ADD COLUMN company_id varchar(32) NULL COMMENT '所属公司ID（permission=company 时使用）' AFTER permission;
