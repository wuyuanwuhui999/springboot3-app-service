# tenant 租户模块接口文档

> 服务名：tenant-service | 端口：3007 | 路径前缀：/service/tenant

## 概述

租户模块：租户列表、租户用户管理（搜索、列表、当前用户）、管理员设置、成员增删。

## 鉴权

所有接口均需 token（网关注入 `X-User-Id`）。

## 接口总览

| 方法 | 接口 | 作用 | 鉴权 |
|------|------|------|------|
| GET | /service/tenant/getTenantList | 租户列表 | 需 |
| GET | /service/tenant/searchTenantUsers | 搜索租户用户 | 需 |
| GET | /service/tenant/getTenantUserList | 租户用户列表 | 需 |
| GET | /service/tenant/getTenantUser | 当前租户用户信息 | 需 |
| PUT | /service/tenant/addAdmin/{tenantId}/{userId} | 设为管理员 | 需 |
| PUT | /service/tenant/cancelAdmin/{tenantId}/{userId} | 取消管理员 | 需 |
| POST | /service/tenant/addTenantUser/{tenantId}/{userId} | 添加租户用户 | 需 |
| DELETE | /service/tenant/deleteTenantUser/{tenantId}/{userId} | 删除租户用户 | 需 |

## 接口详情

### 1. 租户列表
- 接口：`GET /service/tenant/getTenantList`
- 入参：`X-User-Id`（Header）+ Query：`companyId`
- 出参：ResultEntity，data 为租户列表

### 2. 搜索租户用户
- 接口：`GET /service/tenant/searchTenantUsers`
- 入参：`X-User-Id`（Header）+ Query：`companyId`、`tenantId`、`keyword`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为用户列表，`total` 为总数

### 3. 租户用户列表
- 接口：`GET /service/tenant/getTenantUserList`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`、`keyword`（可选）、`pageNum`、`pageSize`
- 出参：ResultEntity，data 为用户列表，`total` 为总数

### 4. 当前租户用户信息
- 接口：`GET /service/tenant/getTenantUser`
- 入参：`X-User-Id`（Header）+ Query：`tenantId`
- 出参：ResultEntity，data 为当前用户在租户中的信息

### 5. 设为管理员
- 接口：`PUT /service/tenant/addAdmin/{tenantId}/{userId}`
- 入参：`X-User-Id`（Header，当前操作人）+ Path：`tenantId`、`userId`（被设置的用户）
- 出参：ResultEntity

### 6. 取消管理员
- 接口：`PUT /service/tenant/cancelAdmin/{tenantId}/{userId}`
- 入参：`X-User-Id`（Header，当前操作人）+ Path：`tenantId`、`userId`
- 出参：ResultEntity

### 7. 添加租户用户
- 接口：`POST /service/tenant/addTenantUser/{tenantId}/{userId}`
- 入参：`X-User-Id`（Header，当前操作人）+ Path：`tenantId`、`userId`
- 出参：ResultEntity

### 8. 删除租户用户
- 接口：`DELETE /service/tenant/deleteTenantUser/{tenantId}/{userId}`
- 入参：`X-User-Id`（Header，当前操作人）+ Path：`tenantId`、`userId`
- 出参：ResultEntity

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. tenant（租户表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(50) | 否 | 主键 |  |
| company_id | varchar(32) | 否 | 主键 |  |
| name | varchar(100) | 否 |  | 租户名称 |
| code | varchar(50) | 否 | 唯一 | 租户编码 |
| description | varchar(255) | 是 |  | 租户描述 |
| status | tinyint | 否 |  | 状态：0-禁用，1-启用 |
| create_date | datetime | 否 |  | 创建时间 |
| update_date | datetime | 是 |  | 更新时间 |
| created_by | varchar(32) | 否 |  | 创建人ID |
| updated_by | varchar(32) | 是 |  | 更新人ID |

### 2. tenant_user（租户用户关联表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 主键ID |
| tenant_id | varchar(50) | 是 | 索引 |  |
| user_id | varchar(50) | 是 | 索引 |  |
| role | tinyint | 否 |  | 角色类型：0-普通用户，1-租户管理员，2-超级管理员 |
| join_date | datetime | 否 |  | 加入时间 |
| create_by | varchar(32) | 否 |  | 创建人ID |
| disabled | int(1) unsigned zerofill | 是 |  | 是否禁用 |

### 3. tenant_user_role（租户用户角色表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 |  |
| tenant_id | varchar(32) | 否 |  |  |
| user_id | varchar(32) | 否 |  |  |
| role_type | int | 否 |  | 0-普通用户 1-管理员 2-超级管理员 |
| disabled | tinyint(1) | 是 |  | 是否禁用 |
| create_time | datetime | 是 |  |  |
| update_time | datetime | 是 |  |  |

