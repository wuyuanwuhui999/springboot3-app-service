# AGENTS.md

本文件是给 AI 智能体（Claude Code、Cursor、Copilot、Hermes 等）的项目接入说明。阅读本文件后，你应能快速理解项目结构、技术栈、模块划分、接口规范与鉴权流程。接口的完整入参/出参文档位于 `docs/api/` 目录。

## 项目概述

- 项目类型：Spring Boot 3.x 多模块微服务（Maven 多模块）
- JDK：21
- Spring Boot：3.4.5
- ORM：MyBatis 3.0.4（动态 SQL 写在 `resources/mapper/*.xml`，禁止在 Mapper 接口用 `@Select` 写复杂 SQL）
- 注册中心 / 配置中心：Nacos
- 网关：Spring Cloud Gateway
- 数据库：MySQL（主库名 `play`），Redis（缓存），MongoDB（网关请求日志）

## 模块列表（服务名 + 端口 + 路径前缀）

| 模块 | 服务名 | 端口 | 网关路径前缀 | 说明 |
|------|--------|------|--------------|------|
| gateway | gateway | 3009 | - | 统一入口，路由转发 + JWT 校验 + 异步请求日志 |
| movie | movie-service | 3001 | /service/movie | 电影模块 |
| music | music-service | 3002 | /service/music | 音乐模块 |
| social | social-service | 3003 | /service/social | 评论/点赞社交模块 |
| circle | circle-service | 3004 | /service/circle | 朋友圈（电影圈/音乐圈）模块 |
| user | user-service | 3005 | /service/user | 用户模块 |
| chat | chat-service | 3006 | /service/chat | 聊天/文档/AI 对话模块 |
| tenant | tenant-service | 3007 | /service/tenant | 租户模块 |
| prompt | prompt-service | 3008 | /service/prompt | 提示词模块 |
| agent | agent-service | 3010 | /service/agent | 智能体模块 |
| company | company-service | 3011 | /service/company | 企业模块 |
| common | - | - | - | 公共模块（工具类、统一返回、公共实体） |

## 架构与调用规范（重要）

1. **所有客户端请求必须通过 gateway（3009）转发**，禁止直接调用业务模块。
2. **路由规则**：URL 格式为 `/service/{模块名}/...`，网关按 `Path=/service/{模块名}/**` 路由到对应 `{模块名}-service`（Nacos 负载均衡 `lb://`）。
3. **鉴权与身份透传**：
   - 网关 `JwtAuthFilter` 读取请求头 `Authorization: Bearer <token>`，用 JWT 解析出 `userId`，放入请求头 `X-User-Id` 后透传给下游业务模块。
   - **业务模块禁止再次做 JWT 校验**，统一通过 `@RequestHeader("X-User-Id") String userId` 获取当前登录人。
   - WebSocket 连接：token 通过查询参数 `?token=...`（或 Sec-WebSocket-Protocol 头 / Authorization 头）传递，网关解析后把 `X-User-Id` 放入查询参数与请求头。
4. **鉴权白名单**（无需 token）：
   - `POST /service/user/register`
   - `POST /service/user/login`
   - `POST /service/user/loginByEmail`
   - `POST /service/user/vertifyUser`
   - `POST /service/user/sendEmailVertifyCode`
   - `POST /service/user/resetPassword`
5. **网关不调用业务模块内部逻辑**，只做路由转发、鉴权、日志。

## 统一接口返回规范

所有接口统一返回 `ResultEntity`（`com.player.common.entity.ResultEntity`），字段如下：

| 字段 | 类型 | 说明 |
|------|------|------|
| data | Object | 业务数据 |
| status | String | `SUCCESS` / `FAIL` / `LOGOUT` |
| msg | String | 提示信息（错误提示或增删改成功提示） |
| total | Long | 记录总数（仅分页查询设置） |
| token | String | 用户凭证（仅登录/注册返回） |

成功用 `ResultUtil.success(data)`，分页用 `ResultUtil.success(data, total)`，失败用 `ResultUtil.fail(data, msg)`。

## 命名规范

- 数据库字段：下划线命名（snake_case），如 `user_name`、`create_time`。
- 接口入参（Request）：驼峰命名（camelCase）。
- 接口出参（Response）：下划线字段自动转为驼峰（MyBatis `mapUnderscoreToCamelCase: true`）。
- 类名 PascalCase，方法/变量 camelCase；文件与类名一致。

## 模块内部标准结构

每个业务模块遵循分层：

```
模块名/src/main/java/com/player/模块名/
├── controller/   # 控制器层（仅处理 HTTP 入参、出参、状态码）
├── entity/       # 数据库模型实体
├── mapper/       # 数据访问层（接口 + resources/mapper/{模块名首字母大写}Mapper.xml）
├── service/      # 服务层接口
└── service/imp/  # 服务层实现（业务流程与事务 @Transactional）
```

## 接口文档索引

完整接口文档在 `docs/api/` 目录，按模块拆分：

- [docs/api/README.md](docs/api/README.md) —— 索引 + 公共约定
- [docs/api/gateway.md](docs/api/gateway.md)
- [docs/api/user.md](docs/api/user.md)
- [docs/api/chat.md](docs/api/chat.md)
- [docs/api/agent.md](docs/api/agent.md)
- [docs/api/circle.md](docs/api/circle.md)
- [docs/api/company.md](docs/api/company.md)
- [docs/api/movie.md](docs/api/movie.md)
- [docs/api/music.md](docs/api/music.md)
- [docs/api/prompt.md](docs/api/prompt.md)
- [docs/api/social.md](docs/api/social.md)
- [docs/api/tenant.md](docs/api/tenant.md)

> 快速定位某个接口：先看 `docs/api/README.md` 的接口总表，再进入对应模块文档查看入参/出参详情。
