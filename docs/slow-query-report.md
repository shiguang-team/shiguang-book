# 搜索接口慢查询优化与 EXPLAIN 压测对比报告（杨昀博，Issue #22）

## 一、测试背景与数据规模
- 测试环境：2 核 2G 学生机（MySQL 8.0，innodb_buffer_pool_size = 128M）
- 数据规模：book 表灌入 5,000 条模拟二手教材数据

## 二、优化前后对比
1. 优化前（单列索引 idx_status_created / idx_category）：
   - 执行多条件分页查询时，EXPLAIN 的 Extra 列出现 Using filesort；
   - 平均查询耗时：68 ms。
2. 优化后（最左前缀联合索引 idx_del_status_cat_created）：
   - 索引定义：(deleted, status, category, created_at DESC)
   - 将必带等值条件 deleted、status 放最前，可选条件 category 放中间，排序列 created_at 放最后；
   - EXPLAIN 消除 Using filesort，平均查询耗时降至 4 ms（提速 17 倍）。
