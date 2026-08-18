# company 企业模块接口文档

> 服务名：company-service | 端口：3011 | 路径前缀：/service/company

## 概述

企业模块：企业列表、企业成员管理、搜索用户、部门、职位。

## 鉴权

所有接口均需 token（网关注入 `X-User-Id`）。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/company/getCompanyList | 用户所属公司列表 | 需 |
| GET | /service/company/getCompanyUsers | 公司成员列表 | 需 |
| POST | /service/company/addUser | 添加用户到公司 | 需 |
| GET | /service/company/searchUsers | 搜索公司用户 | 需 |
| GET | /service/company/getDepartments | 查公司部门 | 需 |
| GET | /service/company/getPositions | 查部门职位 | 需 |

## 接口详情

### 1. 用户所属公司列表
- 接口：`GET /service/company/getCompanyList`
- 作用：查询当前用户所属的公司列表
- 入参：`X-User-Id`（Header）
- 出参：ResultEntity，data 为公司列表

### 2. 公司成员列表
- 接口：`GET /service/company/getCompanyUsers`
- 作用：分页查询公司成员
- 入参：`X-User-Id`（Header）+ Query：`companyId`、`keyword`（可选）、`pageNum`（默认 1）、`pageSize`（默认 10）
- 出参：ResultEntity，data 为成员列表，`total` 为总数

### 3. 添加用户到公司
- 接口：`POST /service/company/addUser`
- 作用：将用户加入公司
- 入参：`X-User-Id`（Header）+ Body（CompanyUserEntity）
- 出参：ResultEntity

### 4. 搜索公司用户
- 接口：`GET /service/company/searchUsers`
- 作用：按关键字模糊搜索公司用户（分页）
- 入参：`X-User-Id`（Header）+ Query：`companyId`、`pageNum`、`pageSize`、`keyword`（可选）
- 出参：ResultEntity，data 为用户列表，`total` 为总数

### 5. 查公司部门
- 接口：`GET /service/company/getDepartments`
- 作用：根据公司 ID 查询所有部门
- 入参：`X-User-Id`（Header）+ Query：`companyId`
- 出参：ResultEntity，data 为部门列表

### 6. 查部门职位
- 接口：`GET /service/company/getPositions`
- 作用：根据部门 ID 查询所有职位
- 入参：`X-User-Id`（Header）+ Query：`departmentId`
- 出参：ResultEntity，data 为职位列表

## 请求体实体字段

**CompanyUserEntity（添加用户到公司）**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户 ID |
| companyId | String | 企业 ID |
| positionId | String | 职位 ID |
| departmentId | String | 部门 ID |
| role | Integer | 角色（2 超管 / 1 管理员 / 0 普通成员） |
| username | String | 用户名 |
| telephone | String | 电话 |
| email | String | 邮箱 |
| status | Integer | 状态（0 禁用 / 1 正常） |
