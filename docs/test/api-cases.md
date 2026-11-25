# Apifox 接口测试用例集（累计 46 条，100% 通过，Issue #19）

> 覆盖认证与防爆破（10 条）、书籍检索与生命周期（14 条）、封面上传（5 条）、留言与权限校验（9 条）以及 v0.2 新增的收藏/未读提醒/注销黑名单（8 条），合计 **46 条** 接口回归用例。

## 一、认证与用户安全模块（10 条）

| 用例编号 | 接口 | 测试场景 | 预期结果 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| TC-01 | `POST /api/auth/register` | 合法手机号、6-20位密码、昵称正常注册 | `code=0`，返回 JWT 与脱敏 `user`（无 `password`） | 通过 |
| TC-02 | `POST /api/auth/register` | 手机号前后携带空格注册（回归 BUG-06） | 后端自动 `trim()`，若已存在返回 `code=409` | 通过 |
| TC-03 | `POST /api/auth/register` | 使用已注册手机号重复注册 | `code=409`，提示“该手机号已注册” | 通过 |
| TC-04 | `POST /api/auth/register` | 手机号格式非法或密码少于 6 位 | `code=400`，参数校验拦截并返回首条错误提示 | 通过 |
| TC-05 | `POST /api/auth/login` | 正确手机号与密码登录 | `code=0`，返回有效 Token 并记录成功尝试 | 通过 |
| TC-06 | `POST /api/auth/login` | 密码错误登录 | `code=401`，记录失败尝试 | 通过 |
| TC-07 | `POST /api/auth/login` | 10 分钟内连续 5 次密码错误后再尝试登录 | `code=429`，触发账号临时锁定 10 分钟 | 通过 |
| TC-08 | `POST /api/auth/login` | 禁用状态（`status=0`）账号登录 | `code=403`，提示“该账号已被禁用” | 通过 |
| TC-09 | `GET /api/auth/captcha` | 获取开发环境辅助验证码 | `code=0`，返回 `key` 与 `code="123456"` | 通过 |
| TC-10 | `GET /api/user/me` | 携带有效 Token / 不带 Token 请求个人资料 | 携带 Token 返回 `code=0`；未登录返回 `code=401` | 通过 |

## 二、书籍发布、多维检索与状态流转模块（14 条）

| 用例编号 | 接口 | 测试场景 | 预期结果 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| TC-11 | `GET /api/books` | 默认参数分页拉取首页在售书籍 | `code=0`，仅返回 `status=1` 且回填 `sellerNickname` | 通过 |
| TC-12 | `GET /api/books` | 翻页至第 2 页（回归 BUG-02） | 返回独立第 2 页记录，与第 1 页无重复 | 通过 |
| TC-13 | `GET /api/books` | 按关键词 `keyword` 搜索书名与描述 | 返回匹配书籍列表 | 通过 |
| TC-14 | `GET /api/books` | 按分类 `category=考研` 筛选 | 仅返回“考研”分类书籍 | 通过 |
| TC-15 | `GET /api/books` | 价格区间 `minPrice=15&maxPrice=35` 与留空边界（回归 BUG-04） | 精准区间过滤，参数留空不报 SQL 错 | 通过 |
| TC-16 | `GET /api/books` | 4 种排序切换（`newest/priceAsc/priceDesc/conditionDesc`） | 返回顺序与排序规则严格一致 | 通过 |
| TC-17 | `GET /api/books/{id}` | 免登录查看在售及已下架书籍详情 | `code=0`，展示完整信息及下架原因 | 通过 |
| TC-18 | `GET /api/books/{id}` | 查询不存在或已逻辑删除的书籍 ID | `code=404`，提示“书籍不存在或已删除” | 通过 |
| TC-19 | `POST /api/books` | 已登录用户发布完整合法书籍 | `code=0`，`status=1`，`sellerId` 为当前用户 | 通过 |
| TC-20 | `POST /api/books` | 发布书籍缺少书名、成色超出 1-10 或价格为 0 | `code=400`，DTO 校验拦截 | 通过 |
| TC-21 | `PUT /api/books/{id}` | 书主编辑书籍 / 非书主越权编辑书籍 | 书主 `code=0`；非书主 `code=403` | 通过 |
| TC-22 | `PUT /api/books/{id}/status` | 书主下架书籍并附带 `reason`；传非法状态 `status=9` | 下架成功保存原因；非法状态返回 `code=400` | 通过 |
| TC-23 | `PUT /api/books/{id}/status` | 书主将已下架书籍恢复上架（`status=1`） | `code=0`，状态恢复为 `1` 且清空 `offShelfReason` | 通过 |
| TC-24 | `DELETE /api/books/{id}` | 书主软删除书籍 / 非书主越权删除 | 书主 `code=0`（`deleted=1`）；非书主 `code=403` | 通过 |

## 三、封面图片上传模块（5 条）

| 用例编号 | 接口 | 测试场景 | 预期结果 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| TC-25 | `POST /api/upload` | 上传 1.8MB 合法 `jpg/png/webp` 封面图 | `code=0`，返回 `/files/yyyyMM/<uuid>.ext` 且可访问 | 通过 |
| TC-26 | `POST /api/upload` | 上传 2.3MB 超限图片（回归 BUG-03） | Nginx 放行至 4MB，后端校验拒绝返回 `code=400` | 通过 |
| TC-27 | `POST /api/upload` | 上传非法扩展名文件（如 `.txt`/`.exe`） | `code=400`，提示只支持 jpg/png/webp | 通过 |
| TC-28 | `POST /api/upload` | 上传空文件 | `code=400`，提示选择图片 | 通过 |
| TC-29 | `POST /api/upload` | 未登录调用上传接口 | `code=401` | 通过 |

## 四、站内留言与个人中心模块（9 条）

| 用例编号 | 接口 | 测试场景 | 预期结果 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| TC-30 | `GET /api/messages?bookId=1` | 免登录查看某本书的公开留言列表 | `code=0`，按时间倒序且带 `senderNickname` | 通过 |
| TC-31 | `POST /api/messages` | 买家对在售书籍发表合法留言 | `code=0`，`receiverId=sellerId`，`isRead=0` | 通过 |
| TC-32 | `POST /api/messages` | 留言超过 200 字（回归 BUG-05） | `code=400`，参数校验拦截不报 500 | 通过 |
| TC-33 | `POST /api/messages` | 对已下架书籍发表留言 | `code=409`，提示“该书籍已下架，无法留言” | 通过 |
| TC-34 | `POST /api/messages` | 卖家给自己的书籍留言 | `code=400`，提示“不能给自己的书留言” | 通过 |
| TC-35 | `DELETE /api/messages/{id}` | 发件人删除自己留言 / 非发件人越权删除 | 本人 `code=0`；他人 `code=403` | 通过 |
| TC-36 | `DELETE /api/messages/{id}` | 删除不存在的留言 ID | `code=404` | 通过 |
| TC-37 | `GET /api/books/mine` | 卖家查询个人中心“我发布的书籍” | `code=0`，包含在售与已下架书籍 | 通过 |
| TC-38 | `GET /api/messages/received` | 卖家查询“我收到的留言” | `code=0`，按时间倒序展示买家留言 | 通过 |

## 五、v0.2 新增功能专项回归（收藏、未读提醒、注销黑名单，8 条）

| 用例编号 | 接口 | 测试场景 | 预期结果 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| TC-39 | `POST /api/favorites/{bookId}` | 买家收藏在售书籍 | `code=0`，写入收藏记录 | 通过 |
| TC-40 | `POST /api/favorites/{bookId}` | 连续/并发双击收藏同一本书（回归 BUG-10） | 幂等返回 `code=0`，不触发唯一索引 500 | 通过 |
| TC-41 | `GET /api/favorites/check/{bookId}` | 已登录查询收藏态 / 未登录查询收藏态 | 已收藏返回 `true`，未登录安全返回 `false` | 通过 |
| TC-42 | `GET /api/favorites` | 卖家软删书籍后买家打开收藏夹（回归 BUG-13） | `code=0`，无空指针，降级显示 `status="DELETED"` | 通过 |
| TC-43 | `DELETE /api/favorites/{bookId}` | 买家取消收藏 / 移除已失效书籍收藏 | `code=0`，列表中不再出现该项 | 通过 |
| TC-44 | `GET /api/messages/unread-count` | 收到买家新留言后轮询未读数 | `code=0`，`unreadCount` 准确递增 | 通过 |
| TC-45 | `PUT /api/messages/read-all` | 卖家查看收到留言触发全量已读后再查未读数 | `code=0`，再次查询 `unreadCount=0` | 通过 |
| TC-46 | `POST /api/auth/logout` | 退出登录后携带原 Token 再次请求 `/api/user/me` | 拦截器命中内存黑名单，返回 HTTP `401`（登录已注销） | 通过 |