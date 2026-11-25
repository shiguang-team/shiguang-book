# 接口文档与联调说明（docs/api.md，Issue #16）

> **接口契约冻结说明**：覆盖 Alpha v0.1 主干链路及 v0.2 新增功能（收藏 `#26`、未读数 `#27`、下架恢复 `#30`、注销黑名单 `#29`）。

## 一、全局约定

- **Base URL**：`/api`（线上由 Nginx 反向代理至后端 `8080` 端口）
- **请求头鉴权**：`Authorization: Bearer <JWT_TOKEN>`（HS256 签名，默认有效期 `7200` 秒）
- **统一响应格式**：
  ```json
  {
    "code": 0,
    "message": "ok",
    "data": {}
  }
  ```
- **常见业务状态码**：
  | code | 含义 | 典型场景 |
  | :--- | :--- | :--- |
  | `0` | 成功 | 正常业务返回 |
  | `400` | 参数错误 | 字段校验失败、文件超 2MB、给自己书籍留言、非法状态值 |
  | `401` | 未登录 / Token 失效 | 未携带 Token、Token 过期、Token 已注销在黑名单中、密码错误 |
  | `403` | 无权限 | 修改/上下架/删除他人书籍、删除他人留言、账号已被禁用 |
  | `404` | 资源不存在 | 书籍、用户或留言不存在/已删除 |
  | `409` | 状态冲突 | 手机号重复注册、对已下架书籍发送留言 |
  | `429` | 请求受限 | 10 分钟内连续 5 次密码错误触发锁定 |
  | `500` | 服务器异常 | 全局异常处理兜底 |

---

## 二、认证模块（`/api/auth`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | 否 | 用户注册并直接签发 Token |
| `POST` | `/api/auth/login` | 否 | 手机号 + 密码登录（5 次失败锁 10 分钟） |
| `POST` | `/api/auth/logout` | 选填 | 退出登录，将有效 Token 写入内存黑名单 |
| `GET` | `/api/auth/captcha` | 否 | 开发环境辅助验证码接口（返回固定码 `123456`） |

- **`POST /api/auth/register` 请求体**：
  `{ "phone": "13800000009", "password": "6-20位", "nickname": "最多20字", "dept": "选填,最多30字" }`
  - 手机号后端自动 `trim()`，正则 `^1[3-9]\d{9}$`，重复返回 `409`。
  - 返回：`{ "token": "...", "user": { "id", "phone", "nickname", "dept", "avatar", "status" } }`（绝不返回 `password`）。
- **`POST /api/auth/login` 请求体**：
  `{ "phone": "13800000001", "password": "123456", "captcha": "123456" }`
  - 10 分钟内连续失败 ≥ 5 次返回 `429`；账号禁用返回 `403`。

---

## 三、用户模块（`/api/user`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/user/me` | 是 | 获取当前登录用户个人资料（脱敏不返回密码） |

---

## 四、书籍模块（`/api/books`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/books` | 否 | 首页书籍列表与多条件分页检索 |
| `GET` | `/api/books/{id}` | 否 | 书籍详情（已下架书籍仍可浏览，已软删返回 404） |
| `GET` | `/api/books/mine` | 是 | 个人中心——当前用户发布的书籍列表 |
| `POST` | `/api/books` | 是 | 发布新书籍 |
| `PUT` | `/api/books/{id}` | 是（书主） | 编辑书籍信息（非本人返回 403） |
| `PUT` | `/api/books/{id}/status` | 是（书主） | 上架/下架状态切换（支持下架原因与恢复上架） |
| `DELETE` | `/api/books/{id}` | 是（书主） | 逻辑删除书籍（`deleted=1`，保留历史留言） |

- **`GET /api/books` 查询参数**：
  - `page`（默认 1）、`size`（默认 12，上限 50）
  - `keyword`（模糊匹配书名或描述）、`category`（`教材` / `考研` / `课外` / `其他`）
  - `minPrice`、`maxPrice`（BigDecimal 价格区间）
  - `sort`：`newest`（默认）/ `priceAsc` / `priceDesc` / `conditionDesc`
  - `onlyOnSale`：`1` 只看在售（默认），`0` 全部
- **`POST /api/books` & `PUT /api/books/{id}` 请求体（`BookDTO`）**：
  `{ "title": "≤60字", "category": "教材", "conditionLevel": 1~10, "price": 0.01~99999, "description": "≤500字", "cover": "/files/..." }`
- **`PUT /api/books/{id}/status` Query 参数**：
  - `status`：`0` 下架 / `1` 上架（其他值返回 `400`）
  - `reason`：选填，下架时记录原因展示在详情页；恢复上架（`status=1`）时自动清空。

---

## 五、封面图片上传模块（`/api/upload`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/upload` | 是 | `multipart/form-data` 上传封面，字段名 `file` |

- 限制：大小 ≤ `2MB`，格式仅允许 `jpg / jpeg / png / webp`；按 `yyyyMM` 分目录并以 UUID 重命名存储。
- 返回：`{ "url": "/files/202611/<uuid>.jpg" }`

---

## 六、站内留言与未读提醒模块（`/api/messages`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/messages?bookId={id}` | 否 | 查询某本书的公开留言列表（按时间倒序） |
| `POST` | `/api/messages` | 是 | 发表留言（`bookId`、`content` ≤ 200 字） |
| `DELETE` | `/api/messages/{id}` | 是（发件人） | 逻辑删除自己发布的留言（非本人返回 403） |
| `GET` | `/api/messages/received` | 是 | 个人中心——查询当前用户收到的全部留言 |
| `GET` | `/api/messages/unread-count` | 是 | 获取当前用户未读留言数 `{ "unreadCount": 3 }` |
| `PUT` | `/api/messages/read-all` | 是 | 将当前用户收到的未读留言全部标为已读 |

- 业务规则校验：书籍不存在返回 `404`；书籍已下架返回 `409`；给自己的书籍留言返回 `400`。

---

## 七、收藏模块（`/api/favorites`）

| 方法 | 路径 | 需登录 | 说明 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/favorites/{bookId}` | 是 | 收藏书籍（唯一索引 + 捕获 `DuplicateKeyException` 幂等防重） |
| `DELETE` | `/api/favorites/{bookId}` | 是 | 取消收藏 |
| `GET` | `/api/favorites` | 是 | 我的收藏列表（`FavoriteVO`） |
| `GET` | `/api/favorites/check/{bookId}` | 选填 | 判断当前用户是否已收藏该书（未登录返回 `false`） |

- **`GET /api/favorites` 降级约定（BUG-13 修复）**：
  - `status` 字段返回枚举值：`ON_SHELF`（在售）/ `OFF_SHELF`（已下架）/ `DELETED`（卖家已逻辑删除）。
  - 当书籍被卖家删除时，不抛出空指针异常，安全降级返回 `title = "【该书籍已被卖家删除】"`、`status = "DELETED"`，前端允许买家一键移除收藏。