-- 拾光二手书 Alpha 现场验收一键重置演示数据集
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE favorite;
TRUNCATE TABLE message;
TRUNCATE TABLE book;
TRUNCATE TABLE login_attempt;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 3 个演示账号（密码均为 123456 的 BCrypt 哈希）
INSERT INTO user (id, phone, password_hash, nickname, department) VALUES
(1, '13800000001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '学长小张（卖家）', '计算机学院'),
(2, '13800000002', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '学妹小李（买家）', '软件学院'),
(3, '13800000003', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '陈同学（围观）', '信息工程学院');

-- 12 本高清教材演示数据
INSERT INTO book (id, seller_id, title, category, condition_level, price, cover_url, status, deleted) VALUES
(1, 1, '深入理解计算机系统（CSAPP 原书第3版）', '专业课教材', 9, 45.00, '/files/demo/book-01.jpg', 'ON_SHELF', 0),
(2, 1, '算法导论（原书第3版）', '专业课教材', 8, 38.00, '/files/demo/book-02.jpg', 'ON_SHELF', 0),
(3, 1, '现代软件工程：构建之法（第3版）', '专业课教材', 10, 29.90, '/files/demo/book-03.jpg', 'ON_SHELF', 0),
(4, 1, '计算机网络：自顶向下方法', '专业课教材', 9, 32.00, '/files/demo/book-04.jpg', 'ON_SHELF', 0),
(5, 1, '数据库系统概念（第7版）', '专业课教材', 8, 40.00, '/files/demo/book-05.jpg', 'ON_SHELF', 0),
(6, 2, '高等数学（同济第七版 上册）', '公共课教材', 9, 15.00, '/files/demo/book-06.jpg', 'ON_SHELF', 0),
(7, 2, '线性代数（同济第六版）', '公共课教材', 9, 12.00, '/files/demo/book-07.jpg', 'ON_SHELF', 0),
(8, 2, '概率论与数理统计（浙大第四版）', '公共课教材', 8, 14.50, '/files/demo/book-08.jpg', 'ON_SHELF', 0),
(9, 2, '新视野大学英语读写教程 3', '外语考试', 9, 18.00, '/files/demo/book-09.jpg', 'ON_SHELF', 0),
(10, 3, '六级词汇词根+联想记忆法', '外语考试', 10, 16.00, '/files/demo/book-10.jpg', 'ON_SHELF', 0),
(11, 3, '王道 2026 计算机组成原理考研复习指导', '考研资料', 9, 25.00, '/files/demo/book-11.jpg', 'ON_SHELF', 0),
(12, 3, '肖秀荣考研政治精讲精练', '考研资料', 8, 22.00, '/files/demo/book-12.jpg', 'OFF_SHELF', 0);
