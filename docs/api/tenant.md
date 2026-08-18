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
