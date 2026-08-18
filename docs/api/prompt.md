# prompt 提示词模块接口文档

> 服务名：prompt-service | 端口：3008 | 路径前缀：/service/prompt

## 概述

提示词模块：提示词的查询、新增、更新、删除、分页列表。

## 鉴权

所有接口均需 token（网关注入 `X-User-Id`）。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/prompt/getPrompt | 查提示词 | 需 |
| DELETE | /service/prompt/deletePrompt/{tenantId}/{id} | 删除提示词 | 需 |
| PUT | /service/prompt/updatePrompt | 更新提示词 | 需 |
| PUT | /service/prompt/insertPrompt | 新增提示词 | 需 |
| GET | /service/prompt/getPromptList | 分页提示词列表 | 需 |

## 接口详情

### 1. 查提示词
- 接口：`GET /service/prompt/getPrompt`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`（必填）、`promptId`（可选）
- 出参：ResultEntity，data 为提示词

### 2. 删除提示词
- 接口：`DELETE /service/prompt/deletePrompt/{tenantId}/{id}`
- 入参：`X-User-Id`（Header）+ Path：`tenantId`、`id`
- 出参：ResultEntity

### 3. 更新提示词
- 接口：`PUT /service/prompt/updatePrompt`
- 入参：`X-User-Id`（Header）+ Body（PromptEntity）
- 出参：ResultEntity

### 4. 新增提示词
- 接口：`PUT /service/prompt/insertPrompt`
- 入参：`X-User-Id`（Header）+ Body（PromptEntity）
- 出参：ResultEntity

### 5. 分页提示词列表
- 接口：`GET /service/prompt/getPromptList`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`、`keyword`（可选）、`pageSize`、`pageNum`
- 出参：ResultEntity，data 为提示词列表，`total` 为总数

## 请求体实体字段

**PromptEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键 ID |
| tenantId | String | 租户 ID |
| prompt | String | 提示词标题/内容 |
| userId | String | 用户 id |
| createTime | Date | 创建时间 |
| updateTime | Date | 更新时间 |
