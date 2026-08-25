# movie 电影模块接口文档

> 服务名：movie-service | 端口：3001 | 路径前缀：/service/movie

## 概述

电影模块：分类、推荐、搜索、演员、播放地址、播放记录、浏览记录、收藏、电影详情、类型相似电影、搜索历史。

## 鉴权

分类/搜索/详情类接口无需 `X-User-Id`（部分无 token 要求）；用户相关接口（记录、收藏、历史）需 token（网关注入 `X-User-Id`）。下表"鉴权"列标"需"表示该接口读取 `X-User-Id`。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/movie/findClassify | 获取分类 | 否 |
| GET | /service/movie/getKeyWord | 按类型获取推荐影片 | 否 |
| GET | /service/movie/getUserMsg | 用户使用天数/关注/记录数 | 需 |
| GET | /service/movie/getAllCategoryByClassify | 按大类查所有小类 | 否 |
| GET | /service/movie/getAllCategoryListByPageName | 按页面查展示小类 | 否 |
| GET | /service/movie/getCategoryList | 查大类中的小类 | 否 |
| GET | /service/movie/getTopMovieList | 按分类取前 20 条 | 否 |
| GET | /service/movie/search | 多条件搜索 | 否 |
| GET | /service/movie/getStar/{movieId} | 演员列表 | 否 |
| GET | /service/movie/getMovieUrl | 播放地址 | 否 |
| GET | /service/movie/getPlayRecord | 播放记录 | 需 |
| POST | /service/movie/savePlayRecord/{movieId} | 保存播放记录 | 需 |
| GET | /service/movie/getViewRecord | 浏览记录 | 需 |
| POST | /service/movie/saveViewRecord/{movieId} | 保存浏览记录 | 需 |
| GET | /service/movie/getFavoriteList | 收藏列表 | 需 |
| POST | /service/movie/saveFavorite/{movieId} | 保存收藏 | 需 |
| DELETE | /service/movie/deleteFavorite/{movieId} | 删除收藏 | 需 |
| GET | /service/movie/isFavorite | 是否已收藏 | 需 |
| GET | /service/movie/getYourLikes | 猜你想看 | 否 |
| GET | /service/movie/getRecommend | 推荐电影 | 否 |
| GET | /service/movie/getMovieDetail/{movieId} | 电影详情 | 否 |
| GET | /service/movie/getMovieListByType | 类型相似电影 | 否 |
| GET | /service/movie/getSearchHistory | 搜索历史 | 需 |

## 接口详情

### 1. 获取分类
- 接口：`GET /service/movie/findClassify`
- 入参：无
- 出参：ResultEntity，data 为分类列表
- 出参示例：
```json
{
  "data": [{"id":1,"classify":"电影","category":"banner","pageName":"首页","sourceName":"本地","status":"1"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 2. 按类型获取推荐影片
- 接口：`GET /service/movie/getKeyWord`
- 入参（Query）：`classify`（分类，如 电影/电视剧）
- 出参：ResultEntity，data 为影片列表
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 3. 用户使用统计
- 接口：`GET /service/movie/getUserMsg`
- 入参：`X-User-Id`（Header）
- 出参：ResultEntity，data 为 {使用天数、关注数、观看记录数、浏览记录数}
- 出参示例：
```json
{
  "data": {"useDays":30,"followCount":1,"viewRecordCount":10,"playRecordCount":5},
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 4. 按大类查所有小类
- 接口：`GET /service/movie/getAllCategoryByClassify`
- 入参（Query）：`classify`
- 出参：ResultEntity，data 为小类列表
- 出参示例：
```json
{
  "data": [{"id":1,"category":"动作","classify":"电影"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 5. 按页面查展示小类
- 接口：`GET /service/movie/getAllCategoryListByPageName`
- 入参（Query）：`pageName`（页面名称）
- 出参：ResultEntity，data 为小类列表
- 出参示例：
```json
{
  "data": [{"id":1,"category":"banner","classify":"电影"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 6. 查大类中的小类
- 接口：`GET /service/movie/getCategoryList`
- 入参（Query）：`classify`、`category`
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

### 7. 按分类取前 20 条
- 接口：`GET /service/movie/getTopMovieList`
- 入参（Query）：`classify`、`category`（可选）
- 出参：ResultEntity，data 为电影列表
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 8. 多条件搜索
- 接口：`GET /service/movie/search`
- 入参（Query）：`classify`、`category`、`label`、`star`、`director`、`keyword`（均可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为电影列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 9. 演员列表
- 接口：`GET /service/movie/getStar/{movieId}`
- 入参（Path）：`movieId`
- 出参：ResultEntity，data 为演员列表
- 出参示例：
```json
{
  "data": [{"id":1,"starName":"吴京","img":"https://example.com/star.jpg","role":"主演","movieId":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 10. 播放地址
- 接口：`GET /service/movie/getMovieUrl`
- 入参（Query）：`movieId`
- 出参：ResultEntity，data 为播放地址
- 出参示例：
```json
{
  "data": [{"id":1,"movieId":1,"label":"第1集","url":"https://example.com/play.m3u8","playGroup":"1","sourceName":"本地"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 11. 播放记录
- 接口：`GET /service/movie/getPlayRecord`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为记录列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"movieId":1,"userId":"uuid","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 12. 保存播放记录
- 接口：`POST /service/movie/savePlayRecord/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
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

### 13. 浏览记录
- 接口：`GET /service/movie/getViewRecord`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": [{"id":1,"movieId":1,"userId":"uuid","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 14. 保存浏览记录
- 接口：`POST /service/movie/saveViewRecord/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
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

### 15. 收藏列表
- 接口：`GET /service/movie/getFavoriteList`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 16. 保存收藏
- 接口：`POST /service/movie/saveFavorite/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
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

### 17. 删除收藏
- 接口：`DELETE /service/movie/deleteFavorite/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
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

### 18. 是否已收藏
- 接口：`GET /service/movie/isFavorite`
- 入参：`X-User-Id`（Header）+ Query：`movieId`
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

### 19. 猜你想看
- 接口：`GET /service/movie/getYourLikes`
- 入参（Query）：`labels`（标签，多个用 / 分隔）、`classify`
- 出参：ResultEntity，data 为电影列表
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 20. 推荐电影
- 接口：`GET /service/movie/getRecommend`
- 入参（Query）：`classify`
- 出参：ResultEntity，data 为电影列表
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 21. 电影详情
- 接口：`GET /service/movie/getMovieDetail/{movieId}`
- 入参（Path）：`movieId`
- 出参：ResultEntity，data 为电影详情
- 出参示例：
```json
{
  "data": {"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"},
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 22. 类型相似电影
- 接口：`GET /service/movie/getMovieListByType`
- 入参（Query）：`types`（类型，多个用空格分隔）、`classify`
- 出参：ResultEntity，data 为电影列表
- 出参示例：
```json
{
  "data": [{"id":1,"movieName":"流浪地球","director":"郭帆","star":"吴京,屈楚萧","type":"科幻","countryLanguage":"中国/汉语","img":"https://example.com/img.jpg","classify":"电影","isRecommend":"1","score":"9.0","duration":"120","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 23. 搜索历史
- 接口：`GET /service/movie/getSearchHistory`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为搜索历史列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":1,"keyword":"流浪地球","userId":"uuid","createTime":"2024-01-01 12:00:00"}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. movie

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| ext_movie_id | varchar(11) | 是 |  | 第三方系统的电影id |
| movie_name | varchar(255) | 是 |  | 电影名称 |
| director | varchar(255) | 是 |  | 导演 |
| star | varchar(1000) | 是 |  | 主演 |
| type | varchar(255) | 是 |  | 类型 |
| country_language | varchar(255) | 是 |  | 国家/语言 |
| viewing_state | varchar(255) | 是 |  | 观看状态	 |
| release_time | varchar(255) | 是 |  | 上映时间 |
| plot | text | 是 |  | 剧情 |
| update_time | date | 是 |  | 更新时间 |
| is_recommend | varchar(4) | 是 |  | 是否推荐，0:不推荐，1:推荐 |
| big_img | varchar(255) | 是 |  | 网络大图 |
| img | varchar(255) | 是 |  | 电影海报 |
| classify | enum('电影','电视剧','动漫','新片库','福利','午夜','恐怖','综艺','其他') | 是 |  | 分类 电影,电视剧,动漫,综艺,新片库,福利,午夜,恐怖,其他 |
| source_name | varchar(255) | 是 |  | 来源名称，本地，骑士影院，爱奇艺 |
| source_url | varchar(255) | 是 |  | 来源地址 |
| create_time | datetime | 是 |  | 创建时间 |
| big_local_img | varchar(255) | 是 |  | 本地大图 |
| local_img | varchar(255) | 是 |  | 本地图片 |
| label | varchar(255) | 是 |  | 标签 |
| original_href | varchar(255) | 是 |  | 源地址 |
| description | varchar(255) | 是 |  | 简单描述 |
| target_href | varchar(255) | 是 |  | 链接地址 |
| use_status | varchar(64) | 是 |  | 0代表未使用，1表示正在使用，是banner和carousel图的才有 |
| score | varchar(255) | 是 |  | 评分 |
| category | varchar(255) | 是 |  | 类目，值为banner首屏，carousel：滚动轮播 |
| ranks | varchar(255) | 是 |  | 排名 |
| douban_url | varchar(255) | 是 |  | 对应豆瓣网的地址 |
| duration | varchar(16) | 是 |  | 播放时长 |
| 
privilege_id | int | 是 |  | 权限 |

### 2. movie_category

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| classify | varchar(64) | 是 |  | 分类 电影,电视剧,动漫,综艺,新片库,福利,午夜,恐怖,其他 |
| category | varchar(255) | 是 |  | 类目，值为banner首屏，carousel：滚动轮播 |
| status | enum('0','1') | 是 |  | 使用状态状态，1表示显示，0表示隐藏 |
| page_name | varchar(255) | 是 |  | 页面名称 |
| source_name | varchar(255) | 是 |  | 来源 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 修改时间 |

### 3. movie_network

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| movie_id | varchar(11) | 是 |  |  |
| movie_name | varchar(255) | 是 |  | 电影名称 |
| director | varchar(255) | 是 |  | 导演 |
| star | varchar(255) | 是 |  | 主演 |
| type | varchar(255) | 是 |  | 类型 |
| country_language | varchar(255) | 是 |  | 国家/语言 |
| viewing_state | varchar(255) | 是 |  | 观看状态	 |
| release_time | varchar(255) | 是 |  | 上映时间 |
| plot | text | 是 |  | 剧情 |
| update_time | date | 是 |  | 更新时间 |
| is_recommend | enum('0','1') | 是 |  | 是否推荐，0:不推荐，1:推荐 |
| img | varchar(255) | 是 |  | 电影海报 |
| classify | varchar(64) | 是 |  | 分类 电影,电视剧,动漫,综艺,新片库,福利,午夜,恐怖,其他 |
| source_name | varchar(255) | 是 |  | 来源名称，本地，骑士影院，爱奇艺 |
| source_url | varchar(255) | 是 |  | 来源地址 |
| create_time | datetime | 是 |  | 创建时间 |
| local_img | varchar(255) | 是 |  | 本地图片 |
| label | varchar(255) | 是 |  | 标签 |
| original_href | varchar(255) | 是 |  | 源地址 |
| description | varchar(3000) | 是 |  | 简单描述 |
| target_href | varchar(3000) | 是 |  | 链接地址 |
| use_status | int | 是 |  | 0代表未使用，1表示正在使用，是banner和carousel图的才有 |
| score | varchar(255) | 是 |  | 评分 |
| category | varchar(255) | 是 |  | 类目，值为banner首屏，carousel：滚动轮播 |
| ranks | varchar(255) | 是 |  | 排名 |
| user_id | varchar(255) | 是 |  | 用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致 |
| douban_url | varchar(255) | 是 |  | 对应豆瓣网的地址 |
| duration | varchar(16) | 是 |  | 播放时长 |
| privilege_id | int | 是 |  | 权限 |

### 4. movie_url

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| movie_name | varchar(255) | 否 |  | 电影名称 |
| movie_id | int | 是 |  | 对应的电影的id |
| href | varchar(255) | 是 |  | 源地址 |
| label | varchar(255) | 是 |  | 集数 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| url | varchar(255) | 是 |  | 播放地址 |
| play_group | varchar(255) | 是 |  | 播放分组，1, 2 |
| source_name | varchar(255) | 是 |  | 来源 |

### 5. movie_stars

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| star_name | varchar(255) | 是 |  | 演员名称 |
| img | varchar(255) | 是 |  | 演员图片地址 |
| local_img | varchar(255) | 是 |  | 演员本地图片地址 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |
| movie_id | int | 是 |  | 对应电影的id |
| role | varchar(255) | 是 |  | 角色 |
| href | varchar(255) | 是 |  | 演员的豆瓣链接地址 |
| works | varchar(255) | 是 |  | 代表作 |

### 6. movie_play_record

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| movie_id | varchar(11) | 是 |  | 电影id |
| user_id | varchar(255) | 是 |  | 用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 7. movie_view_record

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| movie_id | varchar(11) | 是 |  | 电影id |
| user_id | varchar(255) | 是 |  | 用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

### 8. movie_favorite（收藏的电影）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | int | 否 | 主键 | 主键 |
| movie_id | varchar(11) | 是 |  | 电影id |
| user_id | varchar(255) | 是 |  | 用户名，这这个表不需要，为了跟记录叫和收藏表的结构一致 |
| create_time | datetime | 是 |  | 创建时间 |
| update_time | datetime | 是 |  | 更新时间 |

