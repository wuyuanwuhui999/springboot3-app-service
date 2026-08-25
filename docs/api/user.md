# user 用户模块接口文档

> 服务名：user-service | 端口：3005 | 路径前缀：/service/user

## 概述

用户模块：注册、登录、邮箱登录、校验用户、用户信息查询/更新、改密码、头像上传、搜索用户、邮箱验证码、重置密码。

## 鉴权

- 白名单（无需 token）：register / login / loginByEmail / vertifyUser / sendEmailVertifyCode / resetPassword。
- 其余接口需 token，经网关注入 `X-User-Id` 请求头。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/user/getUserData | 查询用户信息 | 需 |
| POST | /service/user/login | 登录 | 白名单 |
| POST | /service/user/register | 注册 | 白名单 |
| POST | /service/user/vertifyUser | 校验用户是否存在 | 白名单 |
| PUT | /service/user/updateUser | 更新用户信息 | 需 |
| PUT | /service/user/updatePassword | 修改密码 | 需 |
| POST | /service/updateAvater | 头像上传 | 需 |
| POST | /service/user/sendEmailVertifyCode | 发送邮箱验证码 | 白名单 |
| POST | /service/user/resetPassword | 重置密码 | 白名单 |
| POST | /service/user/loginByEmail | 邮箱登录 | 白名单 |
| GET | /service/user/searchUsers | 搜索用户 | 需 |

## 接口详情

### 1. 查询用户信息
- 接口：`GET /service/user/getUserData`
- 作用：查询当前登录用户信息
- 入参：`X-User-Id`（Header，可选）
- 出参：ResultEntity，data 为 UserEntity（用户信息）

### 2. 登录
- 接口：`POST /service/user/login`
- 作用：账号密码登录，成功返回 token
- 入参（Body，UserEntity）：`userAccount`（账号）、`password`（密码）
- 出参：ResultEntity，data 为用户信息，`token` 为登录凭证

### 3. 注册
- 接口：`POST /service/user/register`
- 作用：注册新用户
- 入参（Body，UserEntity）：`userAccount`、`password`、`username`（昵称）、`telephone`、`email`、`avater` 等
- 出参：ResultEntity，`token` 为注册后凭证

### 4. 校验用户是否存在
- 接口：`POST /service/user/vertifyUser`
- 作用：注册前校验账号/用户名是否已存在
- 入参（Body，UserEntity）：`userAccount` 或 `username`
- 出参：ResultEntity

### 5. 更新用户信息
- 接口：`PUT /service/user/updateUser`
- 作用：更新当前用户资料
- 入参：`X-User-Id`（Header）+ Body（UserEntity：`username`/`telephone`/`email`/`avater`/`sex`/`birthday`/`sign`/`region` 等）
- 出参：ResultEntity

### 6. 修改密码
- 接口：`PUT /service/user/updatePassword`
- 作用：修改登录密码
- 入参：`X-User-Id`（Header）+ Body（PasswordEntity：`oldPassword` 旧密码、`newPassword` 新密码）
- 出参：ResultEntity

### 7. 头像上传
- 接口：`POST /service/updateAvater`
- 作用：上传头像图片
- 入参：`X-User-Id`（Header）+ `file`（Form，multipart/form-data 文件）
- 出参：ResultEntity
- 注意：该接口路径为 `/service/updateAvater`（不在 `/service/user` 前缀下）

### 8. 发送邮箱验证码
- 接口：`POST /service/user/sendEmailVertifyCode`
- 作用：找回密码时发送验证码邮件
- 入参（Body，MailEntity）：`email`（接收邮箱）、`subject`（主题）、`text`（文本）、`code`（验证码）
- 出参：ResultEntity

### 9. 重置密码
- 接口：`POST /service/user/resetPassword`
- 作用：通过邮箱验证码重置密码
- 入参（Body，ResetPasswordEntity）：`email`、`code`（验证码）、`password`（新密码）
- 出参：ResultEntity

### 10. 邮箱登录
- 接口：`POST /service/user/loginByEmail`
- 作用：邮箱验证码登录
- 入参（Body，MailEntity）：`email`、`code`（验证码）
- 出参：ResultEntity，`token` 为登录凭证

### 11. 搜索用户
- 接口：`GET /service/user/searchUsers`
- 作用：按关键字搜索用户（分页）
- 入参（Query）：`keyword`（关键字）、`companyId`、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为用户列表，`total` 为总数

## 请求体实体字段

**UserEntity**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 用户 uid |
| userAccount | String | 账号 |
| username | String | 昵称 |
| password | String | 密码 |
| telephone | String | 电话 |
| email | String | 邮箱 |
| avater | String | 头像 |
| birthday | String | 出生年月日 |
| sex | String | 性别 |
| sign | String | 个性签名 |
| region | String | 地区 |
| disabled | int | 是否禁用 |
| permission | int | 权限 |

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. user（用户表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 |  | 主键id |
| user_account | varchar(32) | 否 |  | 账号 |
| password | varchar(255) | 否 |  | 密码 |
| create_date | varchar(255) | 否 |  | 创建时间 |
| update_date | datetime | 否 |  | 更新时间 |
| username | varchar(255) | 否 |  | 昵称 |
| telephone | varchar(20) | 否 |  | 电话 |
| email | varchar(255) | 否 |  | 邮箱 |
| avater | varchar(255) | 是 |  | 头像地址 |
| birthday | varchar(16) | 是 |  | 出生年月日 |
| sex | varchar(1) | 是 |  | 性别，0:男，1:女 |
| role | varchar(255) | 是 |  | 角色 |
| sign | varchar(255) | 是 |  | 个性签名 |
| region | varchar(255) | 是 |  | 地区 |
| disabled | int | 是 |  | 是否禁用，0表示不不禁用，1表示禁用 |
| permission | int | 是 |  | 权限大小 |

### 2. login_log（登录日志表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | bigint | 否 | 主键 | 主键ID（自增） |
| user_id | varchar(50) | 是 | 索引 | 用户ID |
| ip | varchar(50) | 是 |  | 登录IP |
| login_type | varchar(50) | 是 |  | 登录类型：register/login/getUserData |
| create_time | datetime | 是 | 索引 | 登录时间 |

