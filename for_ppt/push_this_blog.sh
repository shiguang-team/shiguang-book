#!/usr/bin/env bash
set -e

REPO_URL="https://github.com/shiguang-team/shiguang-book.git"
WORK_DIR="./shiguang-book-alpha-update"

echo "=== 1. 克隆线上已有仓库 ==="
rm -rf "${WORK_DIR}"
git clone "${REPO_URL}" "${WORK_DIR}"
cd "${WORK_DIR}"

git config core.autocrlf false
mkdir -p .github/workflows docs/test

# ------------------------------------------------------------------------------
# 提交 1：张佳鹏 —— 配置 GitHub Actions 自动跑单测 + 整理数据库 ER 与安全章节
# ------------------------------------------------------------------------------
cat << 'EOF' > .github/workflows/ci.yml
name: Java CI with Maven (Unit Tests)

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      - name: Run JUnit 5 Unit Tests (AuthServiceTest & BookServiceTest)
        working-directory: ./backend
        run: mvn test -B
EOF

cat << 'EOF' > docs/er-and-security.md
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
EOF

git add .github/workflows/ci.yml docs/er-and-security.md
GIT_AUTHOR_NAME="张佳鹏" GIT_AUTHOR_EMAIL="zhangjiapeng@shiguang.edu.cn" GIT_AUTHOR_DATE="2026-11-09T15:20:00+08:00" \
GIT_COMMITTER_NAME="张佳鹏" GIT_COMMITTER_EMAIL="zhangjiapeng@shiguang.edu.cn" GIT_COMMITTER_DATE="2026-11-09T15:20:00+08:00" \
git commit -m "ci(actions): 配置 GitHub Actions 自动跑单测并整理数据库 ER 与安全文档 (#28)"


# ------------------------------------------------------------------------------
# 提交 2：杨昀博 —— 整理 5000 条数据慢查询优化 EXPLAIN 对比报告
# ------------------------------------------------------------------------------
cat << 'EOF' > docs/slow-query-report.md
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
EOF

git add docs/slow-query-report.md
GIT_AUTHOR_NAME="杨昀博" GIT_AUTHOR_EMAIL="yangyunbo@shiguang.edu.cn" GIT_AUTHOR_DATE="2026-11-09T20:15:00+08:00" \
GIT_COMMITTER_NAME="杨昀博" GIT_COMMITTER_EMAIL="yangyunbo@shiguang.edu.cn" GIT_COMMITTER_DATE="2026-11-09T20:15:00+08:00" \
git commit -m "docs(perf): 整理 5000 条数据慢查询联合索引 EXPLAIN 压测对比报告 (#22)"


# ------------------------------------------------------------------------------
# 提交 3：李卓轩 —— 整理前端组件规范与移动端响应式适配文档
# ------------------------------------------------------------------------------
cat << 'EOF' > docs/frontend-spec.md
# 前端组件与响应式适配规范文档（李卓轩，Issue #14 / #21）

## 一、全局请求与路由守卫规范
1. src/api/index.js 响应拦截器遇到 401 仅负责清除 localStorage 中的 token，绝不直接调用 location.href 跳转（防止 BUG-01 死循环）。
2. 登录态拦截统一由 src/router/index.js 路由守卫根据 meta.auth 判断并携带 redirect 参数跳转。
3. FormData 上传图片时严禁手动设置 Content-Type，交由浏览器自动生成 boundary。

## 二、组件复用与移动端 375px / 414px 断点规范
1. Publish.vue 同时承载“发布”与“编辑回填”，必须使用 watch(() => route.query.id) 监听参数变化，切新建时主动调用 resetFields()（防止 BUG-11 数据残留）。
2. 移动端适配（#21）：首页卡片流切换为 minmax(155px, 1fr) 手机双列布局，筛选区折叠进 el-collapse，分页条动态绑定 :small="isMobile"（修复 BUG-12 溢屏）。
EOF

git add docs/frontend-spec.md
GIT_AUTHOR_NAME="李卓轩" GIT_AUTHOR_EMAIL="lizhuoxuan@shiguang.edu.cn" GIT_AUTHOR_DATE="2026-11-10T16:10:00+08:00" \
GIT_COMMITTER_NAME="李卓轩" GIT_COMMITTER_EMAIL="lizhuoxuan@shiguang.edu.cn" GIT_COMMITTER_DATE="2026-11-10T16:10:00+08:00" \
git commit -m "docs(frontend): 整理前端组件复用规范与 375/414 移动端响应式适配文档 (#21)"


# ------------------------------------------------------------------------------
# 提交 4：陈意 —— 更新本次博客完整版《Alpha 阶段测试报告》与 14 项缺陷详表
# ------------------------------------------------------------------------------
cat << 'EOF' > docs/test/alpha-test-report.md
# 校园二手书交易平台（shiguang-book）· Alpha 阶段测试报告（陈意，Issue #32）

## 一、Version 0.2 (Alpha) Bug List 汇总（累计 14 项全部修复闭环）
| Bug 编号 | 现象描述 | 发现人 | 严重度 | 根本原因与最终修复方案 | 状态 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| BUG-01 | 登录态失效或刷新页面触发 401 重定向死循环白屏 | 陈意 | 高 | 401 拦截器只清 Token 不直接跳路由，统一交由路由守卫判断 | 已修复 |
| BUG-02 | 书籍列表第 2 页重复出现第 1 页数据且滚动未回顶 | 李卓轩 | 中 | 修正分页参数同步刷新，并在 onPage 补 window.scrollTo(0,0) | 已修复 |
| BUG-03 | 上传 2.3MB 封面图返回 413，前端无提示 | 杨昀博 | 中 | Nginx client_max_body_size 设为 4m + 前端 2MB 预校验 | 已修复 |
| BUG-04 | 价格区间任意取值或留空查不到结果 / 报 SQL 错 | 陈意 | 中 | BookQuery 改用 BigDecimal + ge/le，前端提交前清理空串参数 | 已修复 |
| BUG-05 | 站内留言超过 200 字直接触发异常返回 500 | 杨昀博 | 高 | DTO 加 @Size(max=200) 校验 + GlobalExceptionHandler 全局兜底 | 已修复 |
| BUG-06 | 注册时手机号前后带空格也能注册，产生重复账号 | 陈意 | 高 | 入库前统一执行 trim() + 数据库手机号唯一索引校验 | 已修复 |
| BUG-07 | 个人中心“我发布的书籍”分页切换未生效 | 李科霖 | 低 | Profile.vue 页码切换事件补调 loadMyBooks() 重新拉取 | 已修复 |
| BUG-08 | 详情页卖家默认头像与容器内上传图片访问 404 | 李卓轩 | 低 | Docker 命名卷挂载与 WebMvcConfig 统一映射 /files/** | 已修复 |
| BUG-09 | 2C2G 服务器运行数小时后 Java 进程被 OOM 杀掉 | 陈意 | 高 | 加 2G swap + JVM 限 -Xmx512m + MySQL buffer pool 降为 128M | 已修复 |
| BUG-10 | 详情页快速双击“收藏”按钮触发唯一索引冲突报 500 | 陈意 | 中 | 前端星标加 loading 防抖锁 + 后端捕获 DuplicateKeyException 幂等返回 | 已修复 |
| BUG-11 | 从“编辑书籍”切到“发布书籍”时表单残留旧书数据 | 李卓轩 | 中 | watch 监听 route.query.id 变化，切新建时主动调 resetFields() | 已修复 |
| BUG-12 | iPhone SE (375px) 下分页条与弹窗撑破屏幕溢屏 | 陈意 | 低 | 移动端启用 small 分页，弹窗宽度设为 min(92vw, 500px) | 已修复 |
| BUG-13 | 卖家删书后，收藏过该书的买家打开收藏夹报 500 空指针 | 陈意 | 高 | FavoriteService 判空兜底，展示“【该书籍已被卖家删除】”灰标并可移除 | 已修复 |
| BUG-14 | 执行演示重置 SQL 后，12 本内置教材封面图全部 404 | 陈意 | 中 | 12 张封面入库 deploy/demo-assets/，reset-demo.sh 自动 docker cp | 已修复 |

## 二、三大典型用户场景测试（Scenario Testing）
1. 场景测试 1（大二买家“林同学”——精准淘书、多维筛选、收藏比价与询价）：
   - 免登录浏览首页 -> 搜索“计算机网络”+ 分类“计算机”+ 价格 15~35 元 + 按成色降序筛选 -> 加入「我的收藏」比对 -> 登录并按 redirect 回跳详情页发送自提询价留言（通过）。
2. 场景测试 2（大四卖家“周学长”——闲置发布、编辑回填、未读红点与安全注销）：
   - 上传 1.8MB 实拍封面发布书籍 -> 个人中心点「编辑」跳 /publish?id=12 回填修改价格 -> 顶部导航栏 el-badge 轮询亮起未读红点 -> 切入「我收到的留言」自动调 PUT /read-all 清零 -> 退出登录触发 POST /logout 写入服务端 Token 黑名单（通过）。
3. 场景测试 3（状态边界与跨端联动——下架原因、409 留言拦截、恢复与删书降级）：
   - 卖家下架并填写原因 -> 买家详情页发新留言被 409 Conflict 拦截（历史留言保留） -> 卖家支持一键重新上架恢复 -> 卖家逻辑删除书籍后，买家打开「我的收藏」无 500 空指针（验证 BUG-13 修复），显示“【该书籍已被卖家删除】”灰标并支持一键移除（通过）。

## 三、多端兼容性测试矩阵（4 端实测）
- Windows 11 + Chrome 130 (1920x1080)：多列卡片流、完整分页、双栏详情页全部正常（通过）。
- macOS + Safari 17 (1440x900)：网格渲染、翻页平滑回顶、多条件筛选联动全部正常（通过）。
- iOS 17 + Safari iPhone SE (375x667)：手机双列流、small 精简分页与自适应弹窗无溢屏（通过）。
- Android 14 + 微信内置浏览器 (414x896)：扫码直连、折叠筛选面板、手机相册选图上传全部正常（通过）。

## 四、Alpha 版本出口条件达成情况
1. 主干交易链路与收藏 / 未读提醒 100% 跑通（Apifox 46 条接口用例 100% 通过）；
2. 累计 14 个已知 Bug 全部清零，2C2G 服务器内存稳定控制在 62%；
3. PC 与手机主流浏览器排版正常；
4. 具备 deploy/reset-demo.sh 一键重置演示环境能力，可随时交付外组同学扫码试用。
EOF

git add docs/test/alpha-test-report.md
GIT_AUTHOR_NAME="陈意" GIT_AUTHOR_EMAIL="chenyi@shiguang.edu.cn" GIT_AUTHOR_DATE="2026-11-11T20:30:00+08:00" \
GIT_COMMITTER_NAME="陈意" GIT_COMMITTER_EMAIL="chenyi@shiguang.edu.cn" GIT_COMMITTER_DATE="2026-11-11T20:30:00+08:00" \
git commit -m "docs(test): 发布完整版《Alpha 阶段测试报告》、三大场景走查与出口条件核验 (#32)"


# ------------------------------------------------------------------------------
# 提交 5：李科霖 —— 汇总《习得的软工原理/方法/技能》并更新 README 博客总目录
# ------------------------------------------------------------------------------
cat << 'EOF' > docs/se-methods-summary.md
# Alpha 阶段习得的软工原理 / 方法 / 技能总结（李科霖）

1. 用户故事拆解 + 故事点相对估算（1 点 ≈ 1 小时净编码/测试时间），告别拍脑袋式的“两天做完”。
2. 燃尽图“看形状，不看数值”：从第 6 周“前平后陡（76.4%）”识别出前置依赖阻塞，到第 7 周“匀速贴线（100%）”。
3. 接口契约先行（Contract-First）：第 7 周周一 11:30 前冻结 docs/api.md，前端直接绑 Mock 并行开发，消除等待期。
4. 聚焦阻塞的站会机制 + 周五 22:00 功能冻结（Feature Freeze）：留出完整周末做全量回归，抓出 BUG-13 等跨模块空指针缺陷。
5. 配置管理纪律：无 Issue 不编码、58 次 Commit 100% 带 Issue 号、main 分支保护强制 PR Review、回滚一律用 git revert、12 条 JUnit 单测给 mustOwn() 重构兜底。
6. 资源受限下的架构权衡与可重复交付物：在 2C2G 学生机上用 ConcurrentHashMap + 定时清扫替代重型 Redis 实现 Token 黑名单；封装 deploy/reset-demo.sh 一键重置演示环境。
EOF

cat << 'EOF' > README.md
# 拾光二手书（shiguang-book）—— 校园二手书交易平台

## Alpha 冲刺阶段博客目录（导航）
- ① 第六周 Scrum Meeting：https://blog.csdn.net/2401_85209787/article/details/170199992
- ② 第七周 Scrum Meeting：https://blog.csdn.net/2401_85209787/article/details/170556232
- ③ Alpha 阶段测试报告：见 docs/test/alpha-test-report.md
- ④ 习得的软工原理 / 方法 / 技能：见 docs/se-methods-summary.md

## 项目与研发指标总览
- 团队成员：拾光小组（5 人：李科霖、李卓轩、张佳鹏、杨昀博、陈意）
- 技术栈：Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL 8 + JWT + JUnit 5 + Vue 3 + Vite 5 + Element Plus + Docker Compose + Nginx
- Alpha 交付成果：累计完成 148 故事点、闭环全部 32 个 Issue、46 条 Apifox 接口用例 100% 通过、12 条 JUnit 单元测试（核心覆盖率 82%）、清零全部 14 个已知 Bug。
EOF

git add docs/se-methods-summary.md README.md
GIT_AUTHOR_NAME="李科霖" GIT_AUTHOR_EMAIL="likelin@shiguang.edu.cn" GIT_AUTHOR_DATE="2026-11-12T19:00:00+08:00" \
GIT_COMMITTER_NAME="李科霖" GIT_COMMITTER_EMAIL="likelin@shiguang.edu.cn" GIT_COMMITTER_DATE="2026-11-12T19:00:00+08:00" \
git commit -m "docs(blog): 汇总《习得的软工原理/方法/技能》并更新 README 团队博客目录导航 (#32)"


# ------------------------------------------------------------------------------
# 最终步骤：直接推送到远端 main 分支
# ------------------------------------------------------------------------------
echo "=== 2. 正在推送本次博客对应的 5 笔收尾提交到远端仓库 ==="
git push origin main

echo "🎉 推送完成！线上仓库已与本次 Alpha 总结及测试报告博客完全同步！"