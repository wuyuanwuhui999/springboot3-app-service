# music 音乐模块接口文档

> 服务名：music-service | 端口：3002 | 路径前缀：/service/music

## 概述

音乐模块：关键词推荐、分类、列表、歌手、收藏歌手、播放记录、点赞、搜索、歌手分类、收藏夹。

## 鉴权

大部分接口需 token（网关注入 `X-User-Id`）；`getKeywordMusic`、`getMusicClassify`、`getMusicAuthorCategory` 无需用户信息。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/music/getKeywordMusic | 搜索框默认推荐音乐 | 否 |
| GET | /service/music/getMusicClassify | 音乐分类 | 否 |
| GET | /service/music/getMusicListByClassifyId | 按分类取音乐列表 | 需 |
| GET | /service/music/getMusicAuthorListByCategoryId | 按分类取歌手 | 需 |
| GET | /service/music/getMusicListByAuthorId | 按歌手取专辑 | 需 |
| GET | /service/music/getFavoriteAuthor | 收藏的歌手 | 需 |
| POST | /service/music/insertFavoriteAuthor/{authorId} | 收藏歌手 | 需 |
| DELETE | /service/music/deleteFavoriteAuthor/{authorId} | 取消收藏歌手 | 需 |
| GET | /service/music/getMusicRecord | 播放记录 | 需 |
| POST | /service/music/insertMusicRecord | 插入播放记录 | 需 |
| POST | /service/music/insertMusicLike/{id} | 点赞音乐 | 需 |
| DELETE | /service/music/deleteMusicLike/{id} | 取消点赞音乐 | 需 |
| GET | /service/music/getMusicLike | 点赞的音乐 | 需 |
| GET | /service/music/searchMusic | 搜索音乐 | 需 |
| GET | /service/music/queryMusic | 多条件查询音乐 | 否 |
| GET | /service/music/getMusicAuthorCategory | 歌手分类 | 否 |
| GET | /service/music/getFavoriteDirectory | 收藏夹列表 | 需 |
| POST | /service/music/insertFavoriteDirectory | 创建收藏夹 | 需 |
| DELETE | /service/music/deleteFavoriteDirectory/{favoriteId} | 删除收藏夹 | 需 |
| GET | /service/music/getMusicListByFavoriteId | 收藏夹音乐 | 需 |
| PUT | /service/music/updateFavoriteDirectory | 更新收藏夹名称 | 需 |
| GET | /service/music/isMusicFavorite/{musicId} | 是否已收藏 | 需 |
| POST | /service/music/insertMusicFavorite/{musicId} | 添加到收藏夹 | 需 |
| GET | /service/music/getRecommendMusic | 猜你喜欢（前5条） | 需 |

## 接口详情

### 1. 搜索框默认推荐音乐
- 接口：`GET /service/music/getKeywordMusic`
- 入参：无
- 出参：ResultEntity，data 为音乐列表

### 2. 音乐分类
- 接口：`GET /service/music/getMusicClassify`
- 入参：无
- 出参：ResultEntity，data 为分类列表

### 3. 按分类取音乐列表
- 接口：`GET /service/music/getMusicListByClassifyId`
- 入参：`X-User-Id`（Header）+ Query：`classifyId`、`pageNum`、`pageSize`、`isRedis`（默认 0）
- 出参：ResultEntity，data 为音乐列表，`total` 为总数

### 4. 按分类取歌手
- 接口：`GET /service/music/getMusicAuthorListByCategoryId`
- 入参：`X-User-Id`（Header）+ Query：`categoryId`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为歌手列表

### 5. 按歌手取专辑
- 接口：`GET /service/music/getMusicListByAuthorId`
- 入参：`X-User-Id`（Header）+ Query：`authorId`（可选）、`authorName`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表

### 6. 收藏的歌手
- 接口：`GET /service/music/getFavoriteAuthor`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为歌手列表

### 7. 收藏歌手
- 接口：`POST /service/music/insertFavoriteAuthor/{authorId}`
- 入参：`X-User-Id`（Header）+ Path：`authorId`
- 出参：ResultEntity

### 8. 取消收藏歌手
- 接口：`DELETE /service/music/deleteFavoriteAuthor/{authorId}`
- 入参：`X-User-Id`（Header）+ Path：`authorId`
- 出参：ResultEntity

### 9. 播放记录
- 接口：`GET /service/music/getMusicRecord`
- 入参：`X-User-Id`（Header）+ Query：`startDate`（可选）、`endDate`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为记录列表

### 10. 插入播放记录
- 接口：`POST /service/music/insertMusicRecord`
- 入参：`X-User-Id`（Header）+ Body（MusicRecordEntity：`platform`、`device`、`version`、`musicId`）
- 出参：ResultEntity

### 11. 点赞音乐
- 接口：`POST /service/music/insertMusicLike/{id}`
- 入参：`X-User-Id`（Header）+ Path：`id`（音乐 ID）
- 出参：ResultEntity

### 12. 取消点赞音乐
- 接口：`DELETE /service/music/deleteMusicLike/{id}`
- 入参：`X-User-Id`（Header）+ Path：`id`
- 出参：ResultEntity

### 13. 点赞的音乐
- 接口：`GET /service/music/getMusicLike`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表

### 14. 搜索音乐
- 接口：`GET /service/music/searchMusic`
- 入参：`X-User-Id`（Header）+ Query：`keyword`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表

### 15. 多条件查询音乐
- 接口：`GET /service/music/queryMusic`
- 入参（Query，均可选）：`songName`、`authorName`、`albumName`、`language`、`publishStart`、`label`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表，`total` 为总数

### 16. 歌手分类
- 接口：`GET /service/music/getMusicAuthorCategory`
- 入参：无
- 出参：ResultEntity，data 为歌手分类列表

### 17. 收藏夹列表
- 接口：`GET /service/music/getFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Query：`musicId`
- 出参：ResultEntity，data 为收藏夹列表

### 18. 创建收藏夹
- 接口：`POST /service/music/insertFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Body（MusicFavoriteDirectoryEntity：`name`）
- 出参：ResultEntity

### 19. 删除收藏夹
- 接口：`DELETE /service/music/deleteFavoriteDirectory/{favoriteId}`
- 入参：`X-User-Id`（Header）+ Path：`favoriteId`
- 出参：ResultEntity

### 20. 收藏夹音乐
- 接口：`GET /service/music/getMusicListByFavoriteId`
- 入参：`X-User-Id`（Header）+ Query：`favoriteId`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表

### 21. 更新收藏夹名称
- 接口：`PUT /service/music/updateFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Body（MusicFavoriteDirectoryEntity：`id`、`name`）
- 出参：ResultEntity

### 22. 是否已收藏
- 接口：`GET /service/music/isMusicFavorite/{musicId}`
- 入参：`X-User-Id`（Header）+ Path：`musicId`
- 出参：ResultEntity，data 为 1（已收藏）/ 0（未收藏）

### 23. 添加到收藏夹
- 接口：`POST /service/music/insertMusicFavorite/{musicId}`
- 入参：`X-User-Id`（Header）+ Path：`musicId` + Body（List\<MusicFavoriteEntity\>，每个元素含 `favoriteId`）
- 出参：ResultEntity

### 24. 猜你喜欢
- 接口：`GET /service/music/getRecommendMusic`
- 作用：根据 musicId 或 authorId 推荐音乐（前5条）。传 musicId 时按该歌曲 label（逗号分隔多标签，任一命中）推荐、排除当前歌曲，label 为空则按该歌作者推荐；传 authorId 时按作者推荐。两者互斥。
- 入参：`X-User-Id`（Header）+ Query：`musicId`（可选，与 authorId 互斥）、`authorId`（可选，与 musicId 互斥）
- 出参：ResultEntity，data 为音乐列表（前5条，含 isLike），`total` 为条数

## 请求体实体字段

**MusicRecordEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| platform | String | 平台 |
| device | String | 设备 |
| version | String | app 版本 |
| musicId | int | 音乐 id |

**MusicFavoriteDirectoryEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 收藏夹名称 |
| total | int | 收藏夹总歌曲数 |
| cover | String | 封面 |

**MusicFavoriteEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| favoriteId | Long | 收藏夹 id |
| musicId | Long | 音乐 id |
