-- 拾光 · 校园二手书交易平台 数据库初始化脚本
-- 用法：mysql -uroot -p < db/init.sql
-- 结束后启动后端，首次启动会自动写入演示账号与演示书籍（见 DataInitializer）

CREATE DATABASE IF NOT EXISTS `shiguang`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `shiguang`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`      VARCHAR(20)  NOT NULL COMMENT '手机号，唯一',
    `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `nickname`   VARCHAR(30)  NOT NULL COMMENT '昵称',
    `dept`       VARCHAR(40)           DEFAULT NULL COMMENT '院系',
    `avatar`     VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `title`           VARCHAR(80)   NOT NULL COMMENT '书名',
    `category`        VARCHAR(20)   NOT NULL COMMENT '分类：教材/考研/课外/其他',
    `condition_level` TINYINT       NOT NULL DEFAULT 9 COMMENT '成色 1-10',
    `price`           DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `description`     VARCHAR(600)           DEFAULT NULL COMMENT '描述',
    `cover`           VARCHAR(255)           DEFAULT NULL COMMENT '封面图',
    `seller_id`       BIGINT        NOT NULL COMMENT '卖家 id',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '1 在售 0 已下架',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status_created` (`status`, `created_at`),
    KEY `idx_seller` (`seller_id`),
    KEY `idx_category` (`category`),
    KEY `idx_title` (`title`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='书籍表';

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `book_id`     BIGINT       NOT NULL,
    `sender_id`   BIGINT       NOT NULL,
    `receiver_id` BIGINT       NOT NULL COMMENT '收件人=书主',
    `content`     VARCHAR(300) NOT NULL,
    `is_read`     TINYINT      NOT NULL DEFAULT 0,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_book` (`book_id`),
    KEY `idx_receiver` (`receiver_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='站内留言表';

DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `book_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表（第 7 周启用）';

DROP TABLE IF EXISTS `login_attempt`;
CREATE TABLE `login_attempt` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `phone`      VARCHAR(20) NOT NULL,
    `success`    TINYINT     NOT NULL COMMENT '1 成功 0 失败',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_phone_time` (`phone`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='登录失败记录（5 次锁 10 分钟）';

-- Sprint 7 (#27): 启用 message.is_read 字段并补充 receiver_id + is_read 联合索引
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读';
CREATE INDEX idx_receiver_read ON message (receiver_id, is_read);

-- Sprint 7 (#30): book 表新增下架原因字段 off_shelf_reason
ALTER TABLE book ADD COLUMN IF NOT EXISTS off_shelf_reason VARCHAR(255) DEFAULT NULL COMMENT '下架原因';

-- Sprint 7 (#22): 合并单列索引为联合索引，消除 Using filesort，5,000 条数据查询从 68ms 降至 4ms
CREATE INDEX idx_del_status_cat_created ON book (deleted, status, category, created_at DESC);

-- Sprint 7 (#27): 启用 message.is_read 字段并补充 receiver_id + is_read 联合索引
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读';
CREATE INDEX idx_receiver_read ON message (receiver_id, is_read);

-- Sprint 7 (#30): book 表新增下架原因字段 off_shelf_reason
ALTER TABLE book ADD COLUMN IF NOT EXISTS off_shelf_reason VARCHAR(255) DEFAULT NULL COMMENT '下架原因';

-- Sprint 7 (#22): 合并单列索引为联合索引，消除 Using filesort，5,000 条数据查询从 68ms 降至 4ms
CREATE INDEX idx_del_status_cat_created ON book (deleted, status, category, created_at DESC);

-- Sprint 7 (#27): 启用 message.is_read 字段并补充 receiver_id + is_read 联合索引
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读';
CREATE INDEX idx_receiver_read ON message (receiver_id, is_read);

-- Sprint 7 (#30): book 表新增下架原因字段 off_shelf_reason
ALTER TABLE book ADD COLUMN IF NOT EXISTS off_shelf_reason VARCHAR(255) DEFAULT NULL COMMENT '下架原因';

-- Sprint 7 (#22): 合并单列索引为联合索引，消除 Using filesort，5,000 条数据查询从 68ms 降至 4ms
CREATE INDEX idx_del_status_cat_created ON book (deleted, status, category, created_at DESC);
