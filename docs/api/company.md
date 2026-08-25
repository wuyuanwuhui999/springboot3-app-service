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
- 出参示例：
```json
{
  "data": [{"id":"company-xxx","name":"示例公司","code":"DEMO","description":"公司描述","status":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 2. 公司成员列表
- 接口：`GET /service/company/getCompanyUsers`
- 作用：分页查询公司成员
- 入参：`X-User-Id`（Header）+ Query：`companyId`、`keyword`（可选）、`pageNum`（默认 1）、`pageSize`（默认 10）
- 出参：ResultEntity，data 为成员列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":"uuid","userId":"uuid","companyId":"company-xxx","role":"1","positionId":"pos-xxx","isDefault":1,"status":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 3. 添加用户到公司
- 接口：`POST /service/company/addUser`
- 作用：将用户加入公司
- 入参：`X-User-Id`（Header）+ Body（CompanyUserEntity）
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

### 4. 搜索公司用户
- 接口：`GET /service/company/searchUsers`
- 作用：按关键字模糊搜索公司用户（分页）
- 入参：`X-User-Id`（Header）+ Query：`companyId`、`pageNum`、`pageSize`、`keyword`（可选）
- 出参：ResultEntity，data 为用户列表，`total` 为总数
- 出参示例：
```json
{
  "data": [{"id":"uuid","userAccount":"user123","username":"昵称","telephone":"13800138000","email":"user@example.com","avater":"https://example.com/avatar.jpg","birthday":"1990-01-01","sex":"0","role":"admin","sign":"个性签名","region":"广东","disabled":0,"permission":1}],
  "status": "SUCCESS",
  "msg": null,
  "total": 100,
  "token": null
}
```

### 5. 查公司部门
- 接口：`GET /service/company/getDepartments`
- 作用：根据公司 ID 查询所有部门
- 入参：`X-User-Id`（Header）+ Query：`companyId`
- 出参：ResultEntity，data 为部门列表
- 出参示例：
```json
{
  "data": [{"id":"dept-xxx","companyId":"company-xxx","departmentName":"研发部","role":0}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

### 6. 查部门职位
- 接口：`GET /service/company/getPositions`
- 作用：根据部门 ID 查询所有职位
- 入参：`X-User-Id`（Header）+ Query：`departmentId`
- 出参：ResultEntity，data 为职位列表
- 出参示例：
```json
{
  "data": [{"id":"pos-xxx","departmentId":"dept-xxx","positionName":"工程师"}],
  "status": "SUCCESS",
  "msg": null,
  "total": null,
  "token": null
}
```

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

## 涉及的表结构

> 数据库：MySQL `127.0.0.1:3306/play`（root）。以下为本模块接口读写涉及的表结构。

### 1. company（企业表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(50) | 否 | 主键 | 企业ID |
| name | varchar(100) | 否 |  | 企业名称 |
| code | varchar(50) | 否 | 唯一 | 企业编码 |
| description | varchar(255) | 是 |  | 企业描述 |
| status | tinyint | 否 |  | 状态：0-禁用，1-启用 |
| create_date | datetime | 否 |  | 创建时间 |
| update_date | datetime | 是 |  | 更新时间 |
| created_by | varchar(32) | 否 |  | 创建人ID |
| updated_by | varchar(32) | 是 |  | 更新人ID |

### 2. company_department（企业部门表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 部门ID(UUID) |
| company_id | varchar(50) | 否 | 索引 | 所属企业ID |
| department_name | varchar(50) | 否 |  | 部门名称 |
| description | varchar(255) | 是 |  | 部门描述 |
| create_time | datetime | 否 |  | 创建时间 |
| role | int | 是 |  | 查询部门需要当前登录人的角色 |

### 3. company_position（企业职位表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 职位ID(UUID) |
| position_name | varchar(50) | 否 |  | 职位名称 |
| department_id | varchar(32) | 是 | 索引 | 所属部门ID |
| description | varchar(255) | 是 |  | 职位描述 |
| create_time | datetime | 否 |  | 创建时间 |

### 4. company_user（用户企业关联表）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|-----|------|------|
| id | varchar(32) | 否 | 主键 | 主键ID |
| user_id | varchar(32) | 否 | 索引 | 用户ID |
| company_id | varchar(50) | 否 | 索引 | 企业ID |
| is_default | tinyint | 否 |  | 是否默认企业：0-否，1-是 |
| role | varchar(50) | 是 |  | 在企业中的角色（3：企业老板，2：人事，1:管理员，0：普通成员） |
| position_id | varchar(32) | 是 |  | 职位ID |
| join_date | datetime | 否 |  | 加入时间 |
| status | tinyint | 否 |  | 状态：0-禁用，1-正常 |
| create_by | varchar(32) | 否 |  | 创建人ID |

