# 拾光 · 校园二手书交易平台（Alpha v0.1）

> 软件工程课程 Alpha 阶段成果。一个面向本校学生的二手教材 / 课外书交易 Web 应用：注册登录、发布与下架书籍、按书名与分类检索、书籍详情与站内留言。
>
> 团队：拾光小组（杨昀博、李卓轩、张佳鹏、李科霖、陈意）

---

## 一、技术栈

| 层 | 选型 |
| --- | --- |
| 前端 | Vue 3（`<script setup>`）+ Vite 5 + Element Plus + vue-router 4 + axios |
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5 + Jakarta Validation |
| 鉴权 | JWT（jjwt 0.11.5）+ BCrypt 密码加密 |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose + Nginx 反向代理 |

## 二、目录结构

```
shiguang-book/
├── backend/                        # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/shiguang/book/
│       ├── ShiguangApplication.java
│       ├── common/                 # Result / BizException / 全局异常 / 当前用户
│       ├── config/                 # WebMvc、MyBatis-Plus 分页、演示数据初始化
│       ├── interceptor/            # JWT 解析拦截器
│       ├── util/                   # JwtUtil
│       ├── entity/                 # User / Book / Message / Favorite / LoginAttempt
│       ├── mapper/                 # MyBatis-Plus BaseMapper
│       ├── dto/                    # 入参对象（含校验注解）
│       ├── service/                # 业务逻辑
│       └── controller/             # 接口层
├── frontend/                       # Vue 3 前端
│   └── src/{api,router,views}/
├── db/init.sql                     # 建表脚本
├── deploy/nginx.conf               # Nginx 配置
├── docs/test/                      # 缺陷表、接口用例集
└── docker-compose.yml
```

## 三、本地运行

### 1. 准备数据库

```bash
mysql -uroot -p < db/init.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认连接 `localhost:3306/shiguang`，账号 `root/root`。可用环境变量覆盖：
`MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USER` `MYSQL_PWD` `UPLOAD_DIR` `JWT_SECRET`。

**首次启动会自动写入演示数据**（`config/DataInitializer`）：

| 账号 | 密码 | 身份 |
| --- | --- | --- |
| 13800000001 | 123456 | 杨昀博（计算机学院） |
| 13800000002 | 123456 | 李卓轩（软件学院） |
| 13800000003 | 123456 | 陈意（计算机学院） |

同时插入 8 本演示书籍（含 1 本已下架，用于验证"下架后不可留言"）。

> 演示账号用代码初始化而不是写死在 SQL 里：BCrypt 密文必须是运行时算出来的，写死一旦算错全组都登不上。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```

Vite 已把 `/api`、`/files` 代理到 `localhost:8080`，无需处理跨域。

### 4. 或者一条命令起全套（Docker）

```bash
docker compose up -d --build
# 打开 http://localhost
```

## 四、接口一览

| 方法 | 路径 | 说明 | 登录 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册（返回 token） | 否 |
| POST | `/api/auth/login` | 登录（5 次失败锁 10 分钟） | 否 |
| GET | `/api/auth/captcha` | 开发环境验证码（固定 123456，同时打日志） | 否 |
| GET | `/api/user/me` | 当前用户信息 | 是 |
| GET | `/api/books` | 列表 / 搜索 / 筛选 / 排序 / 分页 | 否 |
| GET | `/api/books/{id}` | 详情（含卖家昵称、院系） | 否 |
| GET | `/api/books/mine` | 我发布的书籍 | 是 |
| POST | `/api/books` | 发布书籍 | 是 |
| PUT | `/api/books/{id}` | 编辑书籍 | 是 |
| PUT | `/api/books/{id}/status?status=0\|1` | 上架 / 下架 | 是 |
| DELETE | `/api/books/{id}` | 删除（软删除，留言保留） | 是 |
| GET | `/api/messages?bookId=` | 某本书的留言 | 否 |
| POST | `/api/messages` | 发表留言（已下架的书会被拒绝） | 是 |
| DELETE | `/api/messages/{id}` | 删除自己的留言 | 是 |
| GET | `/api/messages/received` | 我收到的留言 | 是 |
| POST | `/api/upload` | 上传封面图（≤2MB，jpg/png/webp） | 是 |

统一响应体：`{ "code": 0, "message": "ok", "data": ... }`，`code` 非 0 表示业务错误（400 参数 / 401 未登录 / 403 无权限 / 404 不存在 / 409 冲突 / 429 锁定）。

## 五、关键设计决定（对应测试报告里的坑）

1. **拦截器只解析、不拦人**：`JwtInterceptor` 有 token 就解析塞进 request，需要登录的接口在 Controller 里用 `CurrentUser.get()` 主动检查。这样 GET 公开接口不用维护路径白名单，也避免了 BUG-01 那种"拦截器跳登录页 → 登录页又被拦"的死循环。
2. **手机号入库前 `trim` + 唯一索引**（BUG-06）：否则 `138 0000 0000` 和 `13800000000` 会变成两个账号。
3. **查询条件为空绝不传空串**（BUG-04）：`LambdaQueryWrapper` 上用 `StringUtils.hasText()` 判断，而不是 `!= null`。
4. **图片路径只在一处配置**（BUG-03 / BUG-08）：上传目录由 `shiguang.upload.dir` 决定，静态映射、Nginx alias、Docker 卷共用同一路径。
5. **JVM 堆限制 512m**（BUG-09）：学生机只有 2G，Dockerfile 和 compose 里都写死了 `-Xmx512m`。
6. **逻辑删除**：书籍删除只是 `deleted=1`，历史留言与详情页仍可追溯。

## 六、Alpha 阶段范围

**已做**：注册 / 登录 / 登录失败锁定 / 书籍发布与编辑 / 上下架 / 软删除 / 列表分页 / 关键词搜索 / 分类筛选 / 价格区间 / 排序 / 详情 / 站内留言 / 图片上传 / 个人中心（我发布的、我收到的留言）。

**未做**（第 7 周及以后）：收藏、留言未读提醒、移动端适配、token 黑名单（退出登录真正失效）、单元测试、搜索索引优化、支付与评价。

## 七、已知问题

见 [`docs/test/buglist.md`](docs/test/buglist.md)。当前遗留：BUG-04（价格区间筛选失效）、BUG-07（个人中心分页）、移动端适配未开始、退出登录后 token 仍有效至过期。

## 八、常见问题

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| 启动报 `Communications link failure` | MySQL 没起或库不存在 | 先执行 `db/init.sql`，再确认 `MYSQL_HOST/PORT` |
| 上传图片 413 | Nginx 默认 1MB | 已在 `deploy/nginx.conf` 配 `client_max_body_size 4m`；本地开发改 `application.yml` 的 multipart 限制 |
| 图片 404 | 上传目录与静态映射不一致 | 检查 `UPLOAD_DIR` 环境变量，Docker 下确认卷挂载到 `/app/upload` |
| 登录后刷新掉登录态 | token 未正确带上 | 检查 `localStorage.token`，以及请求头 `Authorization: Bearer xxx` |
| 服务跑一段时间挂掉 | 内存不足 | 加 swap、`-Xmx512m`、MySQL buffer pool 降到 128M |
