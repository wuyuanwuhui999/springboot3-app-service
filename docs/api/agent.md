# agent 智能体模块接口文档

> 服务名：agent-service | 端口：3010 | 路径前缀：/service/agent

## 概述

智能体模块：AI 智能体对话（WebSocket 流式）+ 聊天历史查询。

## 鉴权

均需 token（WebSocket 通过 `?token=` 参数，HTTP 通过 `Authorization` 头，网关注入 `X-User-Id`）。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/agent/getChatHistory | 分页聊天历史 | 需 |
| WS | /service/agent/ws/chat | WebSocket 智能体对话 | 需（token 参数） |

## 接口详情

### 1. 分页聊天历史
- 接口：`GET /service/agent/getChatHistory`
- 作用：查询当前用户的智能体聊天历史
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为历史列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"userId":"uuid","chatId":"chat-xxx","modelId":"deepseek-chat","prompt":"你好","responseContent":"你好！有什么可以帮你？","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 2. WebSocket 智能体对话
- 接口：`WS /service/agent/ws/chat`
- 作用：WebSocket 方式智能体对话（流式）
- 入参：`?token=<token>`（查询参数，网关注入 `X-User-Id`）；消息体通过 send 发送（JSON）
- 出参：流式文本消息
- 出参示例：
（流式文本，非 ResultEntity）示例输出：`你好！我是 AI 助手。`

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. chat_model

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

### 2. chat_history（ai会话记录）

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

