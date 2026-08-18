# social 社交模块接口文档

> 服务名：social-service | 端口：3003 | 路径前缀：/service/social

## 概述

社交模块：评论（一级评论、回复）、点赞/收藏，通用社交能力（电影、文章、朋友圈等复用）。

## 鉴权

查询类接口（getCommentCount、getTopCommentList、getReplyCommentList）无需 `X-User-Id`；写操作与点赞需 token。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/social/getCommentCount | 评论总数 | 否 |
| GET | /service/social/getTopCommentList | 一级评论列表 | 否 |
| POST | /service/social/insertComment | 新增评论 | 需 |
| DELETE | /service/social/deleteComment/{id} | 删除评论 | 需 |
| GET | /service/social/getReplyCommentList | 回复列表 | 否 |
| POST | /service/social/saveLike | 点赞/收藏 | 需 |
| DELETE | /service/social/deleteLike | 取消点赞 | 需 |
| GET | /service/social/isLike | 是否已点赞 | 需 |

## 接口详情

### 1. 评论总数
- 接口：`GET /service/social/getCommentCount`
- 入参（Query）：`relationId`（关联资源 ID）、`type`（资源类型）
- 出参：ResultEntity，data 为评论总数

### 2. 一级评论列表
- 接口：`GET /service/social/getTopCommentList`
- 入参（Query）：`relationId`、`type`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为一级评论列表（含回复），`total` 为总数

### 3. 新增评论
- 接口：`POST /service/social/insertComment`
- 入参：`X-User-Id`（Header）+ Body（CommentEntity：`content`、`parentId`、`topId`、`relationId`、`type`）
- 出参：ResultEntity

### 4. 删除评论
- 接口：`DELETE /service/social/deleteComment/{id}`
- 入参：`X-User-Id`（Header）+ Path：`id`
- 出参：ResultEntity

### 5. 回复列表
- 接口：`GET /service/social/getReplyCommentList`
- 入参（Query）：`topId`（顶级评论 ID）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为回复列表

### 6. 点赞/收藏
- 接口：`POST /service/social/saveLike`
- 入参：`X-User-Id`（Header）+ Body（LikeEntity：`relationId`、`type`）
- 出参：ResultEntity

### 7. 取消点赞
- 接口：`DELETE /service/social/deleteLike`
- 入参：`X-User-Id`（Header）+ Query：`relationId`、`type`
- 出参：ResultEntity

### 8. 是否已点赞
- 接口：`GET /service/social/isLike`
- 入参：`X-User-Id`（Header）+ Query：`relationId`、`type`
- 出参：ResultEntity，data 为 1（已点赞）/ 0（未点赞）

## 请求体实体字段

**CommentEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| content | String | 评论内容 |
| parentId | Long | 父评论 ID（一级评论为 null） |
| topId | Long | 顶级评论 ID |
| relationId | Long | 关联资源 ID |
| type | String | 资源类型 |

**LikeEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| relationId | Long | 关联资源 ID |
| type | String | 资源类型 |
