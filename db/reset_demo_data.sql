-- 拾光二手书 演示数据集重置脚本
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE favorite;
TRUNCATE TABLE message;
TRUNCATE TABLE book;
TRUNCATE TABLE login_attempt;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 3 个演示账号（密码均为 123456）
INSERT INTO user (id, phone, password, nickname, dept, avatar, status) VALUES
(1, '13800000001', '$2a$10$sfBpDxssDR0n/5SjF3pCa.Hpb28pXdF0z3DF.5iIfjb/EZMdx/Ijq', '学长小张（卖家）', '计算机学院', NULL, 1),
(2, '13800000002', '$2a$10$sfBpDxssDR0n/5SjF3pCa.Hpb28pXdF0z3DF.5iIfjb/EZMdx/Ijq', '学妹小李（买家）', '软件学院', NULL, 1),
(3, '13800000003', '$2a$10$sfBpDxssDR0n/5SjF3pCa.Hpb28pXdF0z3DF.5iIfjb/EZMdx/Ijq', '陈同学（围观）', '信息工程学院', NULL, 1);

-- 12 本带封面教材演示数据（匹配 deploy/demo-assets/book-01..12.jpg）
INSERT INTO book (id, seller_id, title, category, condition_level, price, cover, description, status, off_shelf_reason, deleted) VALUES
(1, 1, '深入理解计算机系统（CSAPP 原书第3版）', '教材', 9, 45.00, '/files/demo/book-01.jpg', '计算机专业核心教材，少量铅笔批注', 1, NULL, 0),
(2, 1, '算法导论（原书第3版）', '教材', 8, 38.00, '/files/demo/book-02.jpg', '经典教材，书脊完好无缺页', 1, NULL, 0),
(3, 1, '现代软件工程：构建之法（第3版）', '教材', 10, 29.90, '/files/demo/book-03.jpg', '软工课程用书，九九新', 1, NULL, 0),
(4, 1, '计算机网络：自顶向下方法', '教材', 9, 32.00, '/files/demo/book-04.jpg', '课后重点已标注，适合期末复习', 1, NULL, 0),
(5, 1, '数据库系统概念（第7版）', '教材', 8, 40.00, '/files/demo/book-05.jpg', '成色良好，支持图书馆面交', 1, NULL, 0),
(6, 2, '高等数学（同济第七版 上册）', '教材', 9, 15.00, '/files/demo/book-06.jpg', '公共课教材，附带练习笔记', 1, NULL, 0),
(7, 2, '线性代数（同济第六版）', '教材', 9, 12.00, '/files/demo/book-07.jpg', '无涂画，可小刀', 1, NULL, 0),
(8, 2, '概率论与数理统计（浙大第四版）', '教材', 8, 14.50, '/files/demo/book-08.jpg', '期末复习必备', 1, NULL, 0),
(9, 2, '新视野大学英语读写教程 3', '课外', 9, 18.00, '/files/demo/book-09.jpg', '大二英语教材，附带单词手册', 1, NULL, 0),
(10, 3, '六级词汇词根+联想记忆法', '课外', 10, 16.00, '/files/demo/book-10.jpg', '全新未拆封', 1, NULL, 0),
(11, 3, '王道 2026 计算机组成原理考研复习指导', '考研', 9, 25.00, '/files/demo/book-11.jpg', '408 考研必备，仅前两章有铅笔记录', 1, NULL, 0),
(12, 3, '肖秀荣考研政治精讲精练', '考研', 8, 22.00, '/files/demo/book-12.jpg', '暂已出清（用于演示已下架状态）', 0, '已私下约定校内面交，暂不下架保留记录', 0);