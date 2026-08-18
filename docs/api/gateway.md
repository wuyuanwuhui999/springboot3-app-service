# gateway 网关模块

> 服务名：gateway | 端口：3009 | 统一入口

## 概述

Spring Cloud Gateway 网关，是系统唯一对外入口。职责：路由转发、JWT 校验、异步请求日志（MongoDB）。**网关不暴露业务接口，也不调用业务模块内部逻辑**，只做转发。

## 路由规则

URL 格式 `/service/{模块名}/**`，按前缀路由到 Nacos 中的 `{模块名}-service`（`lb://` 负载均衡）：

| 路由前缀 | 目标服务 | 备注 |
|----------|----------|------|
| /service/movie/** | movie-service | |
| /service/music/** | music-service | |
| /service/user/** | user-service | |
| /service/circle/** | circle-service | |
| /service/social/** | social-service | |
| /service/chat/ws/** | chat-service | WebSocket |
| /service/chat/** | chat-service | |
| /service/tenant/** | tenant-service | |
| /service/prompt/** | prompt-service | |
| /service/agent/ws/** | agent-service | WebSocket |
| /service/agent/** | agent-service | |
| /service/company/** | company-service | |

## 鉴权（JwtAuthFilter，全局过滤器 @Order(-1)）

### HTTP 鉴权流程

1. 白名单路径直接放行（见下表）。
2. 从请求头读取 `Authorization: Bearer <token>`。
3. 用 `JwtToken.parseToken` 解析 token → `UserEntity`。
4. 将 `user.getId()` 写入请求头 `X-User-Id`，转发给下游。
5. 无 token / token 无效 → 返回 401。

### 鉴权白名单

| 方法 | 路径 |
|------|------|
| POST | /service/user/register |
| POST | /service/user/resetPassword |
| POST | /service/user/login |
| POST | /service/user/loginByEmail |
| POST | /service/user/vertifyUser |
| POST | /service/user/sendEmailVertifyCode |

### WebSocket 鉴权

1. token 从查询参数 `?token=...` 获取（也支持 Sec-WebSocket-Protocol 头、Authorization 头）。
2. 解析出 userId 后，将 `X-User-Id` 加入查询参数与请求头，转发给下游 WebSocket 服务。

## 请求日志（LogFilter，全局过滤器 @Order(-100)）

- 为每个请求生成 `X-Request-ID`。
- 异步记录请求/响应到 MongoDB（`log` 库），不阻塞主请求。
- 敏感路径（login/loginByEmail/register）不记录请求体/响应体。

## JWT 配置

- `jwt.secret`（默认 `WCdTBej2ZRhIBXafQbALbAwpJ5A+v1PR4A4IN6+OhnM=`）
- `jwt.expiration`：86400000 ms（24 小时）
