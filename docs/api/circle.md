# circle 朋友圈模块接口文档

> 服务名：circle-service | 端口：3004 | 路径前缀：/service/circle

## 概述

朋友圈（电影圈/音乐圈）模块：发布朋友圈、分页查询列表、文章计数、最近更新数量、WebSocket 广播通知。

## 鉴权

除 WebSocket 外，其余接口均需 token（网关注入 `X-User-Id`）；WebSocket 通过 `?token=` 参数。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/circle/getCircleListByType | 分页朋友圈列表 | 需 |
| GET | /service/circle/getCircleArticleCount | 文章评论/收藏/浏览数 | 需 |
| POST | /service/circle/insertCircle | 发布朋友圈 | 需 |
| GET | /service/circle/getCircleByLastUpdateTime | 最近更新数量 | 需 |
| WS | /service/circle/ws | WebSocket 广播 | 需（token 参数） |

## 接口详情

### 1. 分页朋友圈列表
- 接口：`GET /service/circle/getCircleListByType`
- 作用：按类型（MUSIC/MOVIE）分页查询朋友圈，含点赞、评论嵌套数据
- 入参（Query）：`pageSize`、`pageNum`、`type`（MUSIC 或 MOVIE）
- 出参：ResultEntity，data 为 CircleEntity 列表（含 circleLikes、circleComments），`total` 为总数

### 2. 文章评论/收藏/浏览数
- 接口：`GET /service/circle/getCircleArticleCount`
- 作用：查询某条朋友圈的评论数、收藏数、浏览数
- 入参（Query）：`id`（朋友圈文章 ID）
- 出参：ResultEntity，data 为 `{commentCount, favoriteCount, viewCount}`

### 3. 发布朋友圈
- 接口：`POST /service/circle/insertCircle`
- 作用：发布文字/图片朋友圈（图片 base64 保存到磁盘），并广播 WebSocket 消息
- 入参：`X-User-Id`（Header）+ Body（CircleEntity：`content`、`imgs`、`relationId`、`type`、`permission`）
- 出参：ResultEntity，data 为新记录 ID

### 4. 最近更新数量
- 接口：`GET /service/circle/getCircleByLastUpdateTime`
- 作用：查询指定时间之后新增的朋友圈数量
- 入参（Query）：`lastUpdateTime`、`type`
- 出参：ResultEntity，data 为数量

### 5. WebSocket 广播
- 接口：`WS /service/circle/ws`
- 作用：连接后加入广播池；收到消息广播给所有连接；发布朋友圈时服务端广播"有一条新消息"
- 入参：`?token=<token>`
- 出参：文本消息

## 请求体实体字段

**CircleEntity（发布朋友圈时使用）**

| 字段 | 类型 | 说明 |
|------|------|------|
| content | String | 朋友圈内容 |
| imgs | String | 图片（base64，多张逗号分隔） |
| relationId | Long | 关联音乐 audio_id 或电影 movie_id |
| type | String | 类型（MUSIC/MOVIE） |
| permission | int | 权限（0 不公开 / 1 公开） |

**CircleEntity（列表返回时，含扩展字段）**

列表返回的 data 每项还包含：`id`、`userId`、`username`、`useravater`、`createTime`、`updateTime`、音乐字段（`musicSongName`/`musicAudioId`/`musicAuthorName`/`musicAlbumName`/`musicCover`/`musicPlayUrl`/`musicLocalPlayUrl`/`musicLyrics`）或电影字段（`movieId`/`movieName`/`movieDirector`/`movieStar`/`movieType`/`movieCountryLanguage`/`movieViewingState`/`movieReleaseTime`/`movieImg`/`movieClassify`/`movieLocalImg`/`movieScore`），以及 `circleLikes`（点赞列表）、`circleComments`（评论列表，含嵌套 replyList）。

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. circle

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| relation_id | int | 是 |  | 关联音乐或电影的id |
| content | varchar(3000) | 是 |  | 内容 |
| imgs | varchar(1000) | 是 |  | 图片，多张用分号隔开 |
| type | varchar(255) | 是 |  | 类型 |
| user_id | varchar(32) | 是 |  | 用户id |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| permission | int | 是 |  | 权限，0私密，1公开 |

### 2. circle_record

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| circle_id | int | 是 |  |  |
| user_id | varchar(32) | 是 |  |  |
| create_time | datetime(6) | 是 |  |  |
| update_time | datetime(6) | 是 |  |  |

