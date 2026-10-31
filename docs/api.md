# 接口文档（v0.1）

> 后端启动后即可联调。所有接口返回统一结构：
>
> ```json
> { "code": 0, "message": "ok", "data": {} }
> ```
>
> `code` 含义：0 成功 / 400 参数错误 / 401 未登录或过期 / 403 无权限 / 404 不存在 / 409 状态冲突 / 429 锁定 / 500 服务端异常。
>
> 需要登录的接口在请求头带：`Authorization: Bearer <token>`。

## 一、鉴权

### 1.1 注册 `POST /api/auth/register`

```json
{ "phone": "13800000009", "password": "123456", "nickname": "小林", "dept": "计算机学院" }
```

返回：

```json
{ "code": 0, "message": "ok", "data": { "token": "eyJhbGciOi...", "user": { "id": 4, "nickname": "小林" } } }
```

校验：手机号 `^1[3-9]\d{9}$`；密码 6-20 位；手机号唯一（入库前 `trim`）。

### 1.2 登录 `POST /api/auth/login`

```json
{ "phone": "13800000001", "password": "123456" }
```

同一手机号连续 5 次密码错误，锁定 10 分钟，返回 `code=429`。

### 1.3 开发环境验证码 `GET /api/auth/captcha`

固定返回 `123456`（同时打到后端日志），生产环境需替换为图形验证码。

## 二、书籍

### 2.1 列表 / 搜索 `GET /api/books`

| 参数 | 说明 |
| --- | --- |
| page / size | 页码、每页条数（默认 1 / 12，最大 50） |
| keyword | 匹配书名或描述，模糊查询 |
| category | 教材 / 考研 / 课外 / 其他 |
| minPrice / maxPrice | 价格区间 |
| sort | newest（默认）/ priceAsc / priceDesc |

返回 MyBatis-Plus 的分页对象：`{ records: [], total, current, size, pages }`。
`records` 中每条书籍已回填 `sellerNickname`、`sellerDept`，前端不必再查一次用户。

### 2.2 详情 `GET /api/books/{id}`

已下架的书籍仍可访问（返回 `status=0`），但不可留言。

### 2.3 发布 `POST /api/books`（登录）

```json
{ "title": "数据结构（C语言版）", "category": "考研", "conditionLevel": 9,
  "price": 25.00, "description": "严蔚敏版", "cover": "/files/202610/xxx.jpg" }
```

### 2.4 编辑 `PUT /api/books/{id}`（登录，仅本人）

### 2.5 上下架 `PUT /api/books/{id}/status?status=0|1`（登录，仅本人）

### 2.6 删除 `DELETE /api/books/{id}`（登录，仅本人）

逻辑删除，`deleted=1`；历史留言保留。

### 2.7 我发布的 `GET /api/books/mine`（登录）

## 三、留言

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/messages?bookId=1` | 某本书的留言，含 `senderNickname` |
| POST | `/api/messages` | 发布留言，body：`{ "bookId": 1, "content": "能便宜点吗" }` |
| DELETE | `/api/messages/{id}` | 只能删自己的 |
| GET | `/api/messages/received` | 我收到的留言 |

业务规则：不能给自己的书留言（400）；已下架的书不能新增留言（409）；内容上限 200 字。

## 四、上传

`POST /api/upload`（登录），`multipart/form-data`，字段名 `file`。

- 限制：≤ 2MB，仅 jpg / jpeg / png / webp
- 返回：`{ "url": "/files/202610/8f3c....jpg" }`
- 存储：`${UPLOAD_DIR}/yyyyMM/uuid.ext`，由 `/files/**` 静态映射对外提供

## 五、用户

`GET /api/user/me`（登录）→ 当前用户信息，`password` 字段始终为 `null`。

## 六、联调注意事项

1. 前端 axios 已统一拆包：`code` 非 0 直接抛错并弹提示，业务代码只处理成功分支。
2. 401 由前端拦截器统一处理：清 token → 跳登录页；**不要在业务代码里重复跳转**（否则会出现循环重定向，见 BUG-01）。
3. 上传接口不要手动设置 `Content-Type`，让浏览器带 boundary。
4. 查询参数为空时不要传空串，直接不传该参数（见 BUG-04）。
