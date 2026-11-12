# 数据库 ER 结构与接口安全架构总结（张佳鹏）

## 一、核心数据表（5 张表）关系说明
1. user（用户表）：phone 唯一索引，密码经 BCryptPasswordEncoder 哈希存储。
2. book（书籍表）：seller_id 关联 user.id；启用 @TableLogic 逻辑删除（deleted）；建立联合索引 idx_del_status_cat_created。
3. message（站内留言表）：book_id 关联书籍，冗余 receiver_id 提速“我收到的留言”与未读数（is_read）统计。
4. favorite（收藏表）：uk_user_book (user_id, book_id) 唯一约束防并发重复收藏。
5. login_attempt（登录尝试表）：统计 10 分钟内连续失败次数，满 5 次返回 429 锁定。

## 二、接口安全与 2C2G 轻量级 Token 黑名单
- 归属权校验：写操作统一调用 BookService#mustOwn() 校验当前登录用户是否为书主。
- 注销黑名单：TokenBlacklistService 采用 ConcurrentHashMap 存储已注销 Token 的剩余过期时间戳，配合 @Scheduled 每小时定时清理自然过期项，零外部容器开销。
