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

### 2. WebSocket 智能体对话
- 接口：`WS /service/agent/ws/chat`
- 作用：WebSocket 方式智能体对话（流式）
- 入参：`?token=<token>`（查询参数，网关注入 `X-User-Id`）；消息体通过 send 发送（JSON）
- 出参：流式文本消息
