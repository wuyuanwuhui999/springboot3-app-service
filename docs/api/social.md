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
- 出参示例：
```json
{
  "data": 5,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 2. 一级评论列表
- 接口：`GET /service/social/getTopCommentList`
- 入参（Query）：`relationId`、`type`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为一级评论列表（含回复），`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"content":"很好听","relationId":1,"type":"music","userId":"uuid","parentId":0,"topId":0,"createTime":"2024-01-01 12:00:00","replyList":[]}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 3. 新增评论
- 接口：`POST /service/social/insertComment`
- 入参：`X-User-Id`（Header）+ Body（CommentEntity：`content`、`parentId`、`topId`、`relationId`、`type`）
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": null,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 4. 删除评论
- 接口：`DELETE /service/social/deleteComment/{id}`
- 入参：`X-User-Id`（Header）+ Path：`id`
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": null,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 5. 回复列表
- 接口：`GET /service/social/getReplyCommentList`
- 入参（Query）：`topId`（顶级评论 ID）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为回复列表
- 出参示例：
```json
{
  "data": [{"id":1,"content":"很好听","relationId":1,"type":"music","userId":"uuid","parentId":0,"topId":0,"createTime":"2024-01-01 12:00:00","replyList":[]}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 6. 点赞/收藏
- 接口：`POST /service/social/saveLike`
- 入参：`X-User-Id`（Header）+ Body（LikeEntity：`relationId`、`type`）
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": null,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 7. 取消点赞
- 接口：`DELETE /service/social/deleteLike`
- 入参：`X-User-Id`（Header）+ Query：`relationId`、`type`
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": null,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 8. 是否已点赞
- 接口：`GET /service/social/isLike`
- 入参：`X-User-Id`（Header）+ Query：`relationId`、`type`
- 出参：ResultEntity，data 为 1（已点赞）/ 0（未点赞）
- 出参示例：
```json
{
  "data": 1,
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

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

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. social_comment（社交评论）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| content | varchar(255) | 是 |  | 评论内容 |
| parent_id | int | 是 |  | 评论父级id |
| top_id | int | 是 |  | 一级评论 |
| relation_id | int | 否 |  | 文章id |
| type | varchar(32) | 是 |  | 类型 |
| user_id | varchar(32) | 是 |  | 用户id |
| create_time | datetime | 是 |  | 创建时间 |
| udate_time | datetime | 是 |  | 更新时间 |

### 2. social_like（点赞的影片）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| type | varchar(255) | 是 |  | 类型 |
| relation_id | varchar(11) | 否 |  | 关联id |
| user_id | varchar(255) | 是 |  | 用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

