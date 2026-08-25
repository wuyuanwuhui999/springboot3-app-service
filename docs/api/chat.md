# chat 聊天模块接口文档

> 服务名：chat-service | 端口：3006 | 路径前缀：/service/chat

## 概述

聊天模块：AI 对话（SSE 流式）、聊天历史、文档上传/管理、目录管理、模型管理。

## 鉴权

除 `getModelList` 外，其余接口均需 token（网关注入 `X-User-Id`）。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| POST | /service/chat/chat | AI 对话（流式） | 需 |
| GET | /service/chat/getChatHistory | 分页聊天历史 | 需 |
| GET | /service/chat/getChatHistoryByChatId | 按会话查历史 | 需 |
| GET | /service/chat/getModelList | 模型列表 | 否 |
| POST | /service/chat/uploadDoc/{tenantId}/{directoryId} | 上传文档 | 需 |
| GET | /service/chat/getDocListByDirId | 按目录查文档 | 需 |
| GET | /service/chat/getDocList | 查文档列表 | 需 |
| DELETE | /service/chat/deleteDoc/{docId} | 删除文档 | 需 |
| GET | /service/chat/getDirectoryList | 目录列表 | 需 |
| POST | /service/chat/createDir | 创建目录 | 需 |
| PUT | /service/chat/renameDir | 重命名目录 | 需 |
| PUT | /service/chat/deleteDir/{directoryId} | 删除目录 | 需 |
| POST | /service/chat/addModel | 新增模型 | 需 |
| PUT | /service/chat/updateModel | 更新模型 | 需 |
| DELETE | /service/chat/deleteModel/{modelId} | 删除模型 | 需 |
| WS | /service/chat/ws/chat | WebSocket 聊天 | 需（token 参数） |

## 接口详情

### 1. AI 对话（流式）
- 接口：`POST /service/chat/chat`（`produces = text/html;charset=utf-8`，返回 `Flux<String>` 流式）
- 作用：发起 AI 对话，流式返回结果
- 入参：`X-User-Id`（Header）+ Body（ChatParamsEntity）
- 出参：流式文本（非 ResultEntity）

### 2. 分页聊天历史
- 接口：`GET /service/chat/getChatHistory`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`、`tenantId`
- 出参：ResultEntity，data 为历史列表，`total` 为总数

### 3. 按会话查历史
- 接口：`GET /service/chat/getChatHistoryByChatId`
- 入参：`X-User-Id`（Header）+ Query：`chatId`
- 出参：ResultEntity，data 为该会话消息列表

### 4. 模型列表
- 接口：`GET /service/chat/getModelList`
- 入参：Query：`companyId`（必填）、`keyword`（可选，按模型名模糊搜索）
- 出参：ResultEntity，data 为模型列表

### 5. 上传文档
- 接口：`POST /service/chat/uploadDoc/{tenantId}/{directoryId}`
- 入参：`X-User-Id`（Header）+ Path：`tenantId`、`directoryId` + Form：`file`（文件）
- 出参：ResultEntity

### 6. 按目录查文档
- 接口：`GET /service/chat/getDocListByDirId`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`、`directoryId`
- 出参：ResultEntity，data 为文档列表

### 7. 查文档列表
- 接口：`GET /service/chat/getDocList`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`
- 出参：ResultEntity

### 8. 删除文档
- 接口：`DELETE /service/chat/deleteDoc/{docId}`
- 入参：`X-User-Id`（Header）+ Path：`docId`
- 出参：ResultEntity

### 9. 目录列表
- 接口：`GET /service/chat/getDirectoryList`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`
- 出参：ResultEntity，data 为目录列表

### 10. 创建目录
- 接口：`POST /service/chat/createDir`
- 入参：`X-User-Id`（Header）+ Body（DirectoryEntity：`directory`、`tenantId`）
- 出参：ResultEntity

### 11. 重命名目录
- 接口：`PUT /service/chat/renameDir`
- 入参：`X-User-Id`（Header）+ Body（DirectoryEntity：`id`、`directory`）
- 出参：ResultEntity

### 12. 删除目录
- 接口：`PUT /service/chat/deleteDir/{directoryId}`
- 入参：`X-User-Id`（Header）+ Query：`id`（目录 ID）
- 出参：ResultEntity
- 注意：路径变量 `directoryId` 未使用，实际用 Query 参数 `id`

### 13. 新增模型
- 接口：`POST /service/chat/addModel`
- 入参：`X-User-Id`（Header）+ Query：`companyId` + Body（ChatModelEntity）
- 出参：ResultEntity

### 14. 更新模型
- 接口：`PUT /service/chat/updateModel`
- 入参：`X-User-Id`（Header）+ Query：`companyId` + Body（ChatModelEntity）
- 出参：ResultEntity

### 15. 删除模型（逻辑删除）
- 接口：`DELETE /service/chat/deleteModel/{modelId}`
- 入参：`X-User-Id`（Header）+ Query：`companyId` + Path：`modelId`
- 出参：ResultEntity

### 16. WebSocket 聊天
- 接口：`WS /service/chat/ws/chat`
- 作用：WebSocket 方式 AI 对话（流式）
- 入参：`?token=<token>`（查询参数，由网关注入 `X-User-Id`）；消息体通过 send 发送（JSON：prompt、chatId、modelId、docIds 等）
- 出参：流式文本消息

## 请求体实体字段

**ChatParamsEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| prompt | String | 提示词/问题 |
| docIds | List\<String\> | 文档 id 列表 |
| systemPrompt | String | 系统提示词 |
| chatId | String | 会话 id |
| companyId | String | 公司 id |
| tenantId | String | 租户 id |
| modelId | String | 模型 id |
| showThink | Boolean | 是否深度思考 |
| type | String | 类型（document 文档 / db 数据库） |
| language | String | 语言（zh/cn） |

**DirectoryEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键 |
| directory | String | 目录名称 |
| tenantId | String | 租户 id |

**ChatModelEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键 |
| type | String | 大模型类型（ollama/deepseek/tongyi） |
| apiKey | String | 在线大模型 api_key |
| modelName | String | 模型名称 |
| baseUrl | String | API 基础路径 |
| disabled | Integer | 是否禁用（0 启用 / 1 禁用） |
| companyId | String | 所属公司 id |

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. chat_doc（用户上传的RAG文档）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 文档id |
| tenant_id | varchar(32) | 否 | 索引 | 租户ID |
| directory_id | varchar(255) | 是 |  | 租户id |
| name | varchar(255) | 是 |  | 文档原标题 |
| ext | varchar(255) | 是 |  | 文档格式 |
| user_id | varchar(32) | 是 |  | 用户id |
| create_time | datetime | 是 |  | 更新时间 |
| update_time | datetime | 是 |  | 修改时间 |

### 2. chat_doc_directory（按照目录查询文档）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(64) | 否 | 主键 | 租户id |
| user_id | varchar(64) | 是 |  | 用户id |
| directory | varchar(255) | 否 |  | 目录 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| tenant_id | varchar(32) | 是 |  | 租户id |

### 3. chat_history（ai会话记录）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| user_id | varchar(64) | 是 |  | 用户id |
| tenant_id | varchar(255) | 是 |  | 租户id |
| model_id | varchar(64) | 是 |  | 模型名称 |
| files | varchar(1000) | 是 |  | 文件 |
| chat_id | varchar(128) | 是 |  | 会话id |
| prompt | varchar(10000) | 是 |  | 用户提示词 |
| system_prompt | text | 是 |  | 系统提示词 |
| think_content | text | 是 |  | 思考内容 |
| response_content | text | 是 |  | 正文 |
| content | text | 是 |  | 回复内容 |
| create_time | datetime | 是 |  | 创建时间 |

### 4. chat_model

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 主键 |
| company_id | varchar(32) | 否 |  | 企业id |
| type | varchar(255) | 是 |  | 大模型类型，ollama本地大模型/deepseek/tongyi在线大模型 |
| api_key | varchar(255) | 是 |  | 在线大模型的api_key,ollama本地大模型则为空 |
| model_name | varchar(255) | 是 |  | 模型名称 |
| base_url | varchar(255) | 是 |  | 大模型api路径 |
| disabled | int | 是 |  | 是否禁用，0/1 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| created_by | varchar(255) | 是 |  | 创建人 |

