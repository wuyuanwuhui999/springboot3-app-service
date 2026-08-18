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

### 2. 按类型获取推荐影片
- 接口：`GET /service/movie/getKeyWord`
- 入参（Query）：`classify`（分类，如 电影/电视剧）
- 出参：ResultEntity，data 为影片列表

### 3. 用户使用统计
- 接口：`GET /service/movie/getUserMsg`
- 入参：`X-User-Id`（Header）
- 出参：ResultEntity，data 为 {使用天数、关注数、观看记录数、浏览记录数}

### 4. 按大类查所有小类
- 接口：`GET /service/movie/getAllCategoryByClassify`
- 入参（Query）：`classify`
- 出参：ResultEntity，data 为小类列表

### 5. 按页面查展示小类
- 接口：`GET /service/movie/getAllCategoryListByPageName`
- 入参（Query）：`pageName`（页面名称）
- 出参：ResultEntity，data 为小类列表

### 6. 查大类中的小类
- 接口：`GET /service/movie/getCategoryList`
- 入参（Query）：`classify`、`category`
- 出参：ResultEntity

### 7. 按分类取前 20 条
- 接口：`GET /service/movie/getTopMovieList`
- 入参（Query）：`classify`、`category`（可选）
- 出参：ResultEntity，data 为电影列表

### 8. 多条件搜索
- 接口：`GET /service/movie/search`
- 入参（Query）：`classify`、`category`、`label`、`star`、`director`、`keyword`（均可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为电影列表，`total` 为总数

### 9. 演员列表
- 接口：`GET /service/movie/getStar/{movieId}`
- 入参（Path）：`movieId`
- 出参：ResultEntity，data 为演员列表

### 10. 播放地址
- 接口：`GET /service/movie/getMovieUrl`
- 入参（Query）：`movieId`
- 出参：ResultEntity，data 为播放地址

### 11. 播放记录
- 接口：`GET /service/movie/getPlayRecord`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为记录列表，`total` 为总数

### 12. 保存播放记录
- 接口：`POST /service/movie/savePlayRecord/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
- 出参：ResultEntity

### 13. 浏览记录
- 接口：`GET /service/movie/getViewRecord`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity

### 14. 保存浏览记录
- 接口：`POST /service/movie/saveViewRecord/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
- 出参：ResultEntity

### 15. 收藏列表
- 接口：`GET /service/movie/getFavoriteList`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity

### 16. 保存收藏
- 接口：`POST /service/movie/saveFavorite/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
- 出参：ResultEntity

### 17. 删除收藏
- 接口：`DELETE /service/movie/deleteFavorite/{movieId}`
- 入参：`X-User-Id`（Header）+ Path：`movieId`
- 出参：ResultEntity

### 18. 是否已收藏
- 接口：`GET /service/movie/isFavorite`
- 入参：`X-User-Id`（Header）+ Query：`movieId`
- 出参：ResultEntity，data 为 1（已收藏）/ 0（未收藏）

### 19. 猜你想看
- 接口：`GET /service/movie/getYourLikes`
- 入参（Query）：`labels`（标签，多个用 / 分隔）、`classify`
- 出参：ResultEntity，data 为电影列表

### 20. 推荐电影
- 接口：`GET /service/movie/getRecommend`
- 入参（Query）：`classify`
- 出参：ResultEntity，data 为电影列表

### 21. 电影详情
- 接口：`GET /service/movie/getMovieDetail/{movieId}`
- 入参（Path）：`movieId`
- 出参：ResultEntity，data 为电影详情

### 22. 类型相似电影
- 接口：`GET /service/movie/getMovieListByType`
- 入参（Query）：`types`（类型，多个用空格分隔）、`classify`
- 出参：ResultEntity，data 为电影列表

### 23. 搜索历史
- 接口：`GET /service/movie/getSearchHistory`
- 入参：`X-User-Id`（Header）+ Query：`pageNum`、`pageSize`
- 出参：ResultEntity，data 为搜索历史列表，`total` 为总数
