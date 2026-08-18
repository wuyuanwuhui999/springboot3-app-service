# Spring Boot 微服务接口文档

本目录存放项目全部模块的接口文档。所有接口统一经 `gateway`（端口 3009）转发访问，不直接调用业务模块。

## 公共约定

### 请求入口

- 统一入口：`http://<gateway-host>:3009`
- URL 格式：`/service/{模块名}/{接口路径}`

### 鉴权流程（Gateway → 业务模块）

```
客户端 ──(Authorization: Bearer <token>)──> gateway
  gateway: JwtAuthFilter 解析 token → userId
  gateway: 将 userId 放入请求头 X-User-Id → 转发
业务模块: @RequestHeader("X-User-Id") 读取 userId（不再做 JWT 校验）
```

- HTTP 请求：`Authorization: Bearer <token>` 头。
- WebSocket 连接：`?token=<token>` 查询参数（也支持 Sec-WebSocket-Protocol / Authorization 头）。

### 鉴权白名单（无需 token）

| 方法 | 接口 | 作用 |
|------|------|------|
| POST | /service/user/register | 注册 |
| POST | /service/user/login | 登录 |
| POST | /service/user/loginByEmail | 邮箱登录 |
| POST | /service/user/vertifyUser | 校验用户是否存在 |
| POST | /service/user/sendEmailVertifyCode | 发送邮箱验证码 |
| POST | /service/user/resetPassword | 重置密码 |

除以上白名单外，其余接口均需携带 token。

### 统一返回结构 ResultEntity

```json
{
  "data": {},        // 业务数据
  "status": "SUCCESS", // SUCCESS / FAIL / LOGOUT
  "msg": null,       // 提示信息
  "total": null,     // 分页总记录数
  "token": null      // 登录/注册时返回的凭证
}
```

### 入参位置说明

- `Header`：请求头（`X-User-Id` 由网关注入，前端无需传）
- `Query`：URL 查询参数（如 `?pageNum=1`）
- `Path`：URL 路径参数（如 `/getStar/{movieId}`）
- `Body`：请求体 JSON（POST/PUT 的 `@RequestBody`）
- `Form`：multipart/form-data 文件上传

## 模块接口总表

| 模块 | 服务名 | 端口 | 前缀 | 接口数 | 文档 |
|------|--------|------|------|--------|------|
| gateway | gateway | 3009 | - | -（网关） | [gateway.md](gateway.md) |
| user | user-service | 3005 | /service/user | 11 | [user.md](user.md) |
| chat | chat-service | 3006 | /service/chat | 16 | [chat.md](chat.md) |
| agent | agent-service | 3010 | /service/agent | 1 + WS | [agent.md](agent.md) |
| circle | circle-service | 3004 | /service/circle | 4 + WS | [circle.md](circle.md) |
| company | company-service | 3011 | /service/company | 6 | [company.md](company.md) |
| movie | movie-service | 3001 | /service/movie | 23 | [movie.md](movie.md) |
| music | music-service | 3002 | /service/music | 23 | [music.md](music.md) |
| prompt | prompt-service | 3008 | /service/prompt | 5 | [prompt.md](prompt.md) |
| social | social-service | 3003 | /service/social | 8 | [social.md](social.md) |
| tenant | tenant-service | 3007 | /service/tenant | 8 | [tenant.md](tenant.md) |

## 接口快速索引（按模块）

### user（用户）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/user/getUserData | 查询用户信息 |
| POST | /service/user/login | 登录 |
| POST | /service/user/register | 注册 |
| POST | /service/user/vertifyUser | 校验用户是否存在 |
| PUT | /service/user/updateUser | 更新用户信息 |
| PUT | /service/user/updatePassword | 修改密码 |
| POST | /service/updateAvater | 头像上传 |
| POST | /service/user/sendEmailVertifyCode | 发送邮箱验证码 |
| POST | /service/user/resetPassword | 重置密码 |
| POST | /service/user/loginByEmail | 邮箱登录 |
| GET | /service/user/searchUsers | 搜索用户 |

### chat（聊天/文档/模型）
| 方法 | 接口 | 作用 |
|------|------|------|
| POST | /service/chat/chat | AI 对话（SSE 流式） |
| GET | /service/chat/getChatHistory | 分页聊天历史 |
| GET | /service/chat/getChatHistoryByChatId | 按会话查历史 |
| GET | /service/chat/getModelList | 模型列表 |
| POST | /service/chat/uploadDoc/{tenantId}/{directoryId} | 上传文档 |
| GET | /service/chat/getDocListByDirId | 按目录查文档 |
| GET | /service/chat/getDocList | 查文档列表 |
| DELETE | /service/chat/deleteDoc/{docId} | 删除文档 |
| GET | /service/chat/getDirectoryList | 目录列表 |
| POST | /service/chat/createDir | 创建目录 |
| PUT | /service/chat/renameDir | 重命名目录 |
| PUT | /service/chat/deleteDir/{directoryId} | 删除目录 |
| POST | /service/chat/addModel | 新增模型 |
| PUT | /service/chat/updateModel | 更新模型 |
| DELETE | /service/chat/deleteModel/{modelId} | 删除模型 |
| WS | /service/chat/ws/chat | WebSocket 聊天 |

### agent（智能体）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/agent/getChatHistory | 分页聊天历史 |
| WS | /service/agent/ws/chat | WebSocket 聊天 |

### circle（朋友圈）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/circle/getCircleListByType | 分页朋友圈列表 |
| GET | /service/circle/getCircleArticleCount | 文章评论/收藏/浏览数 |
| POST | /service/circle/insertCircle | 发布朋友圈 |
| GET | /service/circle/getCircleByLastUpdateTime | 最近更新数量 |
| WS | /service/circle/ws | WebSocket 广播 |

### company（企业）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/company/getCompanyList | 用户所属公司列表 |
| GET | /service/company/getCompanyUsers | 公司成员列表 |
| POST | /service/company/addUser | 添加用户到公司 |
| GET | /service/company/searchUsers | 搜索公司用户 |
| GET | /service/company/getDepartments | 查公司部门 |
| GET | /service/company/getPositions | 查部门职位 |

### movie（电影）
23 个接口，见 [movie.md](movie.md)：分类/推荐/搜索/演员/播放地址/播放记录/浏览记录/收藏/详情/搜索历史。

### music（音乐）
23 个接口，见 [music.md](music.md)：关键词/分类/列表/歌手/收藏歌手/播放记录/点赞/搜索/歌手分类/收藏夹。

### prompt（提示词）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/prompt/getPrompt | 查提示词 |
| DELETE | /service/prompt/deletePrompt/{tenantId}/{id} | 删除提示词 |
| PUT | /service/prompt/updatePrompt | 更新提示词 |
| PUT | /service/prompt/insertPrompt | 新增提示词 |
| GET | /service/prompt/getPromptList | 分页提示词列表 |

### social（社交评论/点赞）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/social/getCommentCount | 评论总数 |
| GET | /service/social/getTopCommentList | 一级评论列表 |
| POST | /service/social/insertComment | 新增评论 |
| DELETE | /service/social/deleteComment/{id} | 删除评论 |
| GET | /service/social/getReplyCommentList | 回复列表 |
| POST | /service/social/saveLike | 点赞/收藏 |
| DELETE | /service/social/deleteLike | 取消点赞 |
| GET | /service/social/isLike | 是否已点赞 |

### tenant（租户）
| 方法 | 接口 | 作用 |
|------|------|------|
| GET | /service/tenant/getTenantList | 租户列表 |
| GET | /service/tenant/searchTenantUsers | 搜索租户用户 |
| GET | /service/tenant/getTenantUserList | 租户用户列表 |
| GET | /service/tenant/getTenantUser | 当前租户用户信息 |
| PUT | /service/tenant/addAdmin/{tenantId}/{userId} | 设为管理员 |
| PUT | /service/tenant/cancelAdmin/{tenantId}/{userId} | 取消管理员 |
| POST | /service/tenant/addTenantUser/{tenantId}/{userId} | 添加租户用户 |
| DELETE | /service/tenant/deleteTenantUser/{tenantId}/{userId} | 删除租户用户 |
