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
- 出参示例：
```json
{
  "data": {"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0},
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 2. 音乐分类
- 接口：`GET /service/music/getMusicClassify`
- 入参：无
- 出参：ResultEntity，data 为分类列表
- 出参示例：
```json
{
  "data": [{"id":1,"classifyName":"热门","cover":"https://example.com/cover.jpg","classifyRank":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 3. 按分类取音乐列表
- 接口：`GET /service/music/getMusicListByClassifyId`
- 入参：`X-User-Id`（Header）+ Query：`classifyId`、`pageNum`、`pageSize`、`isRedis`（默认 0）
- 出参：ResultEntity，data 为音乐列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 4. 按分类取歌手
- 接口：`GET /service/music/getMusicAuthorListByCategoryId`
- 入参：`X-User-Id`（Header）+ Query：`categoryId`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为歌手列表
- 出参示例：
```json
{
  "data": [{"id":1,"authorId":6051570,"authorName":"周杰伦","avatar":"https://example.com/avatar.jpg","categoryId":1,"rank":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 5. 按歌手取专辑
- 接口：`GET /service/music/getMusicListByAuthorId`
- 入参：`X-User-Id`（Header）+ Query：`authorId`（可选）、`authorName`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 6. 收藏的歌手
- 接口：`GET /service/music/getFavoriteAuthor`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为歌手列表
- 出参示例：
```json
{
  "data": [{"id":1,"authorId":6051570,"authorName":"周杰伦","avatar":"https://example.com/avatar.jpg","categoryId":1,"rank":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 7. 收藏歌手
- 接口：`POST /service/music/insertFavoriteAuthor/{authorId}`
- 入参：`X-User-Id`（Header）+ Path：`authorId`
- 出参：ResultEntity
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

### 8. 取消收藏歌手
- 接口：`DELETE /service/music/deleteFavoriteAuthor/{authorId}`
- 入参：`X-User-Id`（Header）+ Path：`authorId`
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

### 9. 播放记录
- 接口：`GET /service/music/getMusicRecord`
- 入参：`X-User-Id`（Header）+ Query：`startDate`（可选）、`endDate`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为记录列表
- 出参示例：
```json
{
  "data": [{"id":1,"musicId":1,"userId":"uuid","platform":"app","version":"1.0.0","device":"iPhone","times":3}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 10. 插入播放记录
- 接口：`POST /service/music/insertMusicRecord`
- 入参：`X-User-Id`（Header）+ Body（MusicRecordEntity：`platform`、`device`、`version`、`musicId`）
- 出参：ResultEntity
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

### 11. 点赞音乐
- 接口：`POST /service/music/insertMusicLike/{id}`
- 入参：`X-User-Id`（Header）+ Path：`id`（音乐 ID）
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

### 12. 取消点赞音乐
- 接口：`DELETE /service/music/deleteMusicLike/{id}`
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

### 13. 点赞的音乐
- 接口：`GET /service/music/getMusicLike`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 14. 搜索音乐
- 接口：`GET /service/music/searchMusic`
- 入参：`X-User-Id`（Header）+ Query：`keyword`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 15. 多条件查询音乐
- 接口：`GET /service/music/queryMusic`
- 入参（Query，均可选）：`songName`、`authorName`、`albumName`、`language`、`publishStart`、`label`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 16. 歌手分类
- 接口：`GET /service/music/getMusicAuthorCategory`
- 入参：无
- 出参：ResultEntity，data 为歌手分类列表
- 出参示例：
```json
{
  "data": [{"id":1,"categoryName":"华语","rank":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 17. 收藏夹列表
- 接口：`GET /service/music/getFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Query：`musicId`
- 出参：ResultEntity，data 为收藏夹列表
- 出参示例：
```json
{
  "data": [{"id":1,"name":"我的收藏","userId":"uuid","total":10,"checked":0,"cover":"https://example.com/cover.jpg"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 18. 创建收藏夹
- 接口：`POST /service/music/insertFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Body（MusicFavoriteDirectoryEntity：`name`）
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": {"id":1,"name":"我的收藏","userId":"uuid","total":10,"checked":0,"cover":"https://example.com/cover.jpg"},
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 19. 删除收藏夹
- 接口：`DELETE /service/music/deleteFavoriteDirectory/{favoriteId}`
- 入参：`X-User-Id`（Header）+ Path：`favoriteId`
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

### 20. 收藏夹音乐
- 接口：`GET /service/music/getMusicListByFavoriteId`
- 入参：`X-User-Id`（Header）+ Query：`favoriteId`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为音乐列表
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 21. 更新收藏夹名称
- 接口：`PUT /service/music/updateFavoriteDirectory`
- 入参：`X-User-Id`（Header）+ Body（MusicFavoriteDirectoryEntity：`id`、`name`）
- 出参：ResultEntity
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

### 22. 是否已收藏
- 接口：`GET /service/music/isMusicFavorite/{musicId}`
- 入参：`X-User-Id`（Header）+ Path：`musicId`
- 出参：ResultEntity，data 为 1（已收藏）/ 0（未收藏）
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

### 23. 添加到收藏夹
- 接口：`POST /service/music/insertMusicFavorite/{musicId}`
- 入参：`X-User-Id`（Header）+ Path：`musicId` + Body（List\<MusicFavoriteEntity\>，每个元素含 `favoriteId`）
- 出参：ResultEntity
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

### 24. 猜你喜欢
- 接口：`GET /service/music/getRecommendMusic`
- 作用：根据 musicId 或 authorId 推荐音乐（前5条）。传 musicId 时按该歌曲 label（逗号分隔多标签，任一命中）推荐、排除当前歌曲，label 为空则按该歌作者推荐；传 authorId 时按作者推荐。两者互斥。
- 入参：`X-User-Id`（Header）+ Query：`musicId`（可选，与 authorId 互斥）、`authorId`（可选，与 musicId 互斥）
- 出参：ResultEntity，data 为音乐列表（前5条，含 isLike），`total` 为条数
- 出参示例：
```json
{
  "data": [{"id":1,"songName":"晴天","authorId":"6051570","authorName":"周杰伦","albumName":"叶惠美","cover":"https://example.com/cover.jpg","playUrl":"https://example.com/play.mp3","label":"流行,伤感","isHot":1,"isLike":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": 5,
  "token": null
}
```

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

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. music（音乐主表id）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| album_id | int | 是 |  | 歌曲id |
| song_name | varchar(1000) | 是 |  | 歌曲名称 |
| author_id | varchar(255) | 是 |  | 歌手id |
| author_name | varchar(255) | 是 |  | 作者名称 |
| album_name | varchar(255) | 是 |  | 专辑名称 |
| version | varchar(255) | 是 |  | 版本 |
| language | varchar(255) | 是 |  | 语言 |
| publish_date | datetime(6) | 是 |  | 发布日期 |
| wide_audio_id | int | 是 |  | 宽度音频id |
| is_publish | int | 是 |  | 是否发布 |
| big_pack_id | int | 是 |  | 大型集合id |
| final_id | int | 是 |  | 最终id |
| audio_id | int | 是 |  | 音频id |
| similar_audio_id | int | 是 |  | 相似的音乐id |
| is_hot | int | 是 |  | 是否热门 |
| album_audio_id | int | 是 |  | 歌曲音频id |
| audio_group_id | int | 是 |  | 专辑id |
| cover | varchar(255) | 是 |  | 歌曲图片 |
| play_url | varchar(255) | 是 |  | 网络播放地址 |
| local_play_url | varchar(255) | 是 |  | 本地播放地址 |
| source_name | varchar(255) | 是 |  | 播放源 |
| source_url | varchar(1000) | 是 |  | 播放地址 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| label | varchar(255) | 是 |  | 标签 |
| lyrics | text | 是 |  | 歌词 |
| permission | int | 是 |  | 播放权限 |

### 2. music_like

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 |  主键 |
| music_id | int | 否 |  |  |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| user_id | varchar(255) | 是 |  | 用户id |

### 3. music_classify

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| classify_id | int | 是 |  |  |
| music_id | int | 是 |  | 歌曲id |
| audio_rank | int | 是 |  | 歌曲排名，数值越大越靠前 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 4. music_classify_relation

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| classify_name | varchar(255) | 是 |  | 标签 |
| permission | int | 是 |  | 权限 |
| classify_rank | int | 是 |  | 分类排序，数值越大越靠前 |
| cover | varchar(255) | 是 |  | 图标 |
| disabled | int | 是 |  | 是否启用 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 5. music_favorite_directory

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键id |
| name | varchar(255) | 是 |  | 收藏夹名称 |
| user_id | varchar(255) | 否 |  | 用户id |
| create_time | datetime | 否 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 6. music_favorite_list

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键id |
| music_id | int | 是 |  | 音乐id |
| favorite_id | int | 是 | 索引 | 收藏夹id |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 7. music_record

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| music_id | int | 是 |  | 音乐id |
| user_id | varchar(64) | 是 |  | 用户id |
| platform | varchar(255) | 是 |  | 平台 |
| device | varchar(255) | 是 |  | 设备型号 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| version | varchar(255) | 是 |  | app版本 |

### 8. music_author_category（音乐歌手分类）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| category_name | varchar(255) | 是 |  | 分类名称 |
| rank | int | 是 |  | 排名 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| disabled | int | 是 |  | 是否禁用 0:启用，1禁用 |

### 9. music_authors（歌手表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| author_id | int | 是 |  | 歌手id |
| author_name | varchar(255) | 是 |  | 歌手名称 |
| category_id | int | 是 |  | 分类id |
| is_publish | int | 是 |  | 是否发布 |
| avatar | varchar(255) | 是 |  | 头像 |
| type | int | 是 |  | 类型 |
| country | varchar(255) | 是 |  | 国家 |
| birthday | varchar(255) | 是 |  | 生日 |
| identity | int | 是 |  | 身份 |
| rank | int | 是 |  | 歌手排名 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 修改时间 |

### 10. music_author_like（用户喜欢的歌手）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 |  主键 |
| author_id | int | 否 |  | 歌手id |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| user_id | varchar(255) | 是 |  | 用户id |

