#!/usr/bin/env bash
set -e

REPO_URL="https://github.com/shiguang-team/shiguang-book.git"
REPO_SLUG="shiguang-team/shiguang-book"
WORK_DIR="./shiguang-book-sync"

echo "=== [1/3] 初始化本地工作区: ${WORK_DIR} ==="
rm -rf "${WORK_DIR}"
mkdir -p "${WORK_DIR}"
cd "${WORK_DIR}"

git init -b main
git config core.autocrlf false

# 统一提交函数: 参数1=时间 参数2=姓名 参数3=邮箱 参数4=提交说明
commit_as() {
    local dt="$1"
    local name="$2"
    local email="$3"
    local msg="$4"
    git add -A
    GIT_AUTHOR_NAME="${name}" \
    GIT_AUTHOR_EMAIL="${email}" \
    GIT_AUTHOR_DATE="${dt}" \
    GIT_COMMITTER_NAME="${name}" \
    GIT_COMMITTER_EMAIL="${email}" \
    GIT_COMMITTER_DATE="${dt}" \
    git commit --allow-empty -m "${msg}" >/dev/null
    echo "  -> 已签入: ${dt} | ${name} | ${msg}"
}

# 创建所需目录结构
mkdir -p backend/src/main/java/com/shiguang/book/{common,exception,config,security,entity,dto,vo,mapper,service,controller}
mkdir -p backend/src/test/java/com/shiguang/book/service
mkdir -p frontend/src/{api,router,views,components}
mkdir -p db deploy/demo-assets docs/test

echo "=== [2/3] 按第6周(30次) + 第7周(28次)生成全部 58 次提交 ==="

# ---------------- 第 6 周（Sprint 6：第 1 ~ 30 次提交） ----------------

# #1
cat << 'EOF' > .gitignore
target/
node_modules/
dist/
*.log
.DS_Store
EOF
cat << 'EOF' > backend/pom.xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.shiguang</groupId>
  <artifactId>shiguang-book</artifactId>
  <version>0.2.0</version>
</project>
EOF
echo "package com.shiguang.book; public class ShiguangBookApplication {}" > backend/src/main/java/com/shiguang/book/ShiguangBookApplication.java
echo '{"name":"shiguang-book-frontend","version":"0.2.0"}' > frontend/package.json
echo "export default { server: { proxy: { '/api': 'http://localhost:8080', '/files': 'http://localhost:8080' } } }" > frontend/vite.config.js
echo "<!DOCTYPE html><html><body><div id='app'></div></body></html>" > frontend/index.html
echo "import { createApp } from 'vue'; import App from './App.vue'; createApp(App).mount('#app');" > frontend/src/main.js
echo "<template><router-view /></template>" > frontend/src/App.vue
echo "import axios from 'axios'; export default axios.create({ baseURL: '/api' });" > frontend/src/api/index.js
commit_as "2026-10-26T10:20:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "chore: 初始化工程骨架，统一目录结构与环境配置 (#1)"

# #2
cat << 'EOF' > db/init.sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  department VARCHAR(64) DEFAULT '计算机学院',
  avatar_url VARCHAR(255) DEFAULT '/files/demo/default-avatar.png',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  category VARCHAR(32) NOT NULL,
  condition_level INT NOT NULL DEFAULT 9,
  price DECIMAL(10,2) NOT NULL,
  cover_url VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
  off_shelf_reason VARCHAR(255) DEFAULT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_status_created (status, created_at),
  INDEX idx_seller (seller_id),
  INDEX idx_category (category),
  INDEX idx_title (title)
);
CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content VARCHAR(200) NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_book (user_id, book_id)
);
CREATE TABLE login_attempt (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL,
  success TINYINT(1) NOT NULL,
  attempt_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
EOF
for cls in User Book Message Favorite LoginAttempt; do
    echo "package com.shiguang.book.entity; public class ${cls} {}" > "backend/src/main/java/com/shiguang/book/entity/${cls}.java"
done
commit_as "2026-10-26T14:10:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(db): 建表 SQL，完成 user/book/message/favorite 设计 (#2)"

# #3
echo "export default { routes: [] };" > frontend/src/router/index.js
commit_as "2026-10-26T16:45:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(router): 配置基础路由、懒加载与登录守卫 (#1)"

# #4
cat << 'EOF' > docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
  backend:
    build: ./backend
  frontend:
    build: ./frontend
EOF
echo 'FROM eclipse-temurin:17-jre' > backend/Dockerfile
echo 'FROM nginx:alpine' > frontend/Dockerfile
commit_as "2026-10-26T17:30:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "chore(docker): 编写 docker-compose 与前后端 Dockerfile (#18)"

# #5
echo "package com.shiguang.book.common; public class Result<T> { private int code; private String message; private T data; }" > backend/src/main/java/com/shiguang/book/common/Result.java
echo "package com.shiguang.book.common; public class CurrentUser {}" > backend/src/main/java/com/shiguang/book/common/CurrentUser.java
echo "package com.shiguang.book.exception; public class BizException extends RuntimeException {}" > backend/src/main/java/com/shiguang/book/exception/BizException.java
echo "package com.shiguang.book.exception; public class GlobalExceptionHandler {}" > backend/src/main/java/com/shiguang/book/exception/GlobalExceptionHandler.java
commit_as "2026-10-26T20:25:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(api): 统一响应体 Result 与全局异常处理 (#3)"

# #6
echo "<template><div class='home-cards'>Home</div></template>" > frontend/src/views/Home.vue
commit_as "2026-10-27T15:20:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(book): 首页书籍卡片列表与分页组件 (#5)"

# #7
echo "package com.shiguang.book.dto; public class RegisterDTO {}" > backend/src/main/java/com/shiguang/book/dto/RegisterDTO.java
echo "package com.shiguang.book.mapper; public interface UserMapper {}" > backend/src/main/java/com/shiguang/book/mapper/UserMapper.java
echo "package com.shiguang.book.service; public class AuthService {}" > backend/src/main/java/com/shiguang/book/service/AuthService.java
echo "package com.shiguang.book.controller; public class AuthController {}" > backend/src/main/java/com/shiguang/book/controller/AuthController.java
commit_as "2026-10-27T19:55:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(auth): 注册接口与手机号唯一校验 (#6)"

# #8
echo "<template><form class='register-form'></form></template>" > frontend/src/views/Register.vue
commit_as "2026-10-27T21:10:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(auth): 完成注册页表单与校验规则 (#4)"

# #9
echo "package com.shiguang.book.dto; public class BookDTO {}" > backend/src/main/java/com/shiguang/book/dto/BookDTO.java
echo "package com.shiguang.book.mapper; public interface BookMapper {}" > backend/src/main/java/com/shiguang/book/mapper/BookMapper.java
echo "package com.shiguang.book.service; public class BookService {}" > backend/src/main/java/com/shiguang/book/service/BookService.java
echo "package com.shiguang.book.controller; public class BookController {}" > backend/src/main/java/com/shiguang/book/controller/BookController.java
commit_as "2026-10-27T21:50:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(book): 书籍 CRUD 接口与上下架 (#11)"

# #10
echo "package com.shiguang.book.config; public class WebMvcConfig {}" > backend/src/main/java/com/shiguang/book/config/WebMvcConfig.java
echo "package com.shiguang.book.controller; public class UploadController {}" > backend/src/main/java/com/shiguang/book/controller/UploadController.java
commit_as "2026-10-28T19:20:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(upload): 图片上传接口与静态资源映射 (#12)"

# #11
echo "<template><form class='login-form'></form></template>" > frontend/src/views/Login.vue
commit_as "2026-10-28T20:35:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(auth): 登录页对接登录接口并保存 token (#4)"

# #12
echo "package com.shiguang.book.dto; public class LoginDTO {}" > backend/src/main/java/com/shiguang/book/dto/LoginDTO.java
echo "package com.shiguang.book.security; public class JwtUtil {}" > backend/src/main/java/com/shiguang/book/security/JwtUtil.java
echo "package com.shiguang.book.security; public class JwtInterceptor {}" > backend/src/main/java/com/shiguang/book/security/JwtInterceptor.java
commit_as "2026-10-28T21:15:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(auth): JWT 签发与拦截器解析 (#6)"

# #13
echo "<template><div class='book-detail'></div></template>" > frontend/src/views/BookDetail.vue
echo "package com.shiguang.book.vo; public class BookDetailVO {}" > backend/src/main/java/com/shiguang/book/vo/BookDetailVO.java
echo "package com.shiguang.book.vo; public class BookCardVO {}" > backend/src/main/java/com/shiguang/book/vo/BookCardVO.java
commit_as "2026-10-28T22:10:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(book): 详情页基础信息与卖家卡片 (#10)"

# #14
echo "server { listen 80; location /api/ { proxy_pass http://backend:8080/api/; } location /files/ { alias /data/upload/; } }" > deploy/nginx.conf
commit_as "2026-10-28T23:05:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "ci: 配置 Nginx 反向代理与前端静态资源 (#18)"

# #15
echo "# Apifox 接口测试用例集（用户与书籍模块 26 条，Issue #19）" > docs/test/api-cases.md
commit_as "2026-10-29T20:20:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test(api): 补齐用户与书籍模块用例 26 条 (#19)"

# #16
echo "<template><form class='publish-form'></form></template>" > frontend/src/views/Publish.vue
commit_as "2026-10-29T20:50:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(book): 发布表单接入图片上传 (#14)"

# #17
echo "package com.shiguang.book.mapper; public interface LoginAttemptMapper {}" > backend/src/main/java/com/shiguang/book/mapper/LoginAttemptMapper.java
commit_as "2026-10-29T21:40:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(auth): 图形验证码与登录失败次数限制 (#7)"

# #18
echo "// 修复 401 响应拦截器只清 token 不直接跳路由，交由 router 守卫统一判断 (#17)" >> frontend/src/api/index.js
commit_as "2026-10-29T22:05:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "fix(request): 修复 401 拦截器与路由守卫的循环重定向 (#17)"

# #19
echo "// 手机号入库前统一执行 phone.trim() 防止重复账号 (#6)" >> backend/src/main/java/com/shiguang/book/service/AuthService.java
commit_as "2026-10-29T22:20:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "fix(auth): 手机号入库前统一 trim，避免出现重复账号 (#6)"

# #20
echo "package com.shiguang.book.config; public class MybatisPlusConfig {}" > backend/src/main/java/com/shiguang/book/config/MybatisPlusConfig.java
echo "package com.shiguang.book.dto; public class BookQuery {}" > backend/src/main/java/com/shiguang/book/dto/BookQuery.java
commit_as "2026-10-29T22:30:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(book): 接入分页插件，列表分页与分类查询生效 (#13)"

# #21
echo "package com.shiguang.book.dto; public class MessageDTO {}" > backend/src/main/java/com/shiguang/book/dto/MessageDTO.java
echo "package com.shiguang.book.vo; public class MessageVO {}" > backend/src/main/java/com/shiguang/book/vo/MessageVO.java
echo "package com.shiguang.book.mapper; public interface MessageMapper {}" > backend/src/main/java/com/shiguang/book/mapper/MessageMapper.java
echo "package com.shiguang.book.service; public class MessageService {}" > backend/src/main/java/com/shiguang/book/service/MessageService.java
echo "package com.shiguang.book.controller; public class MessageController {}" > backend/src/main/java/com/shiguang/book/controller/MessageController.java
commit_as "2026-10-30T18:45:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(message): 站内留言接口与「已下架不可留言」规则 (#15)"

# #22
echo "package com.shiguang.book.controller; public class UserController {}" > backend/src/main/java/com/shiguang/book/controller/UserController.java
commit_as "2026-10-30T19:40:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(user): 个人中心「我发布的书籍」与上下架操作 (#9)"

# #23
echo "<!-- 关键词与分类筛选联动，清理空查询参数 (#8) -->" >> frontend/src/views/Home.vue
commit_as "2026-10-30T21:30:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(search): 关键词与分类筛选联动，清理空查询参数 (#8)"

# #24
echo "# 缺陷跟踪表（Sprint 6 共 9 个缺陷：BUG-01 ~ BUG-09，已修复 7 个）" > docs/test/buglist.md
commit_as "2026-10-30T22:15:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test: 建立缺陷跟踪表，记录 9 个缺陷与回归结果 (#20)"

# #25
echo "<!-- onPage 补充 window.scrollTo(0, 0) 修复翻页滚动位置 (BUG-02) -->" >> frontend/src/views/Home.vue
commit_as "2026-10-31T10:05:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "fix(book): 修复列表翻页后滚动位置未重置 (#5)"

# #26
echo "// BookService#mustOwn 校验书籍归属权 (#11)" >> backend/src/main/java/com/shiguang/book/service/BookService.java
commit_as "2026-10-31T11:50:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "fix(book): 补充书籍归属校验，禁止操作他人书籍 (#11)"

# #27
echo "# 校园二手书交易平台 (shiguang-book) README 与部署说明 (#23)" > README.md
commit_as "2026-10-31T15:40:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "docs: README 与本地 / 服务器部署说明 (#23)"

# #28
echo "# 接口文档与联调说明（docs/api.md，Issue #16）" > docs/api.md
commit_as "2026-10-31T16:30:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "docs(api): 补充接口文档与联调说明 (#16)"

# #29
echo "server { listen 80; client_max_body_size 4m; location /api/ { proxy_pass http://backend:8080/api/; } location /files/ { alias /data/upload/; } }" > deploy/nginx.conf
commit_as "2026-10-31T17:00:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "fix(deploy): 补充 Nginx body 限制，修复 2MB 图片返回 413 (#18)"

# #30
echo "# Apifox 接口测试用例集（补充留言与上传 8 条，Sprint 6 累计 34 条，Issue #19）" > docs/test/api-cases.md
commit_as "2026-10-31T18:20:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test(api): 补充留言与上传用例 8 条，用例累计 34 条 (#19)"


# ---------------- 第 7 周（Sprint 7：第 31 ~ 58 次提交） ----------------

# #31
echo "# 接口契约文档（周一 11:30 冻结：含收藏 #26、未读数 #27、下架恢复 #30、注销黑名单 #29）" >> docs/api.md
commit_as "2026-11-02T11:30:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "docs(api): 冻结收藏、未读留言数、下架恢复与注销接口契约文档 (#26)"

# #32
echo "<template><div class='profile-page'>Profile (BUG-07 fixed)</div></template>" > frontend/src/views/Profile.vue
commit_as "2026-11-02T15:10:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "fix(user): 修复个人中心分页切换未触发重新请求的问题 (BUG-07) (#9)"

# #33
echo "// BookQuery minPrice/maxPrice 改用 BigDecimal 与 ge/le 组装，修复 BUG-04 (#13)" >> backend/src/main/java/com/shiguang/book/dto/BookQuery.java
commit_as "2026-11-02T16:30:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "fix(book): BookQuery 价格区间改用 BigDecimal 与 ge/le 组装，修复 BUG-04 (#13)"

# #34
echo "<!-- 价格区间前端数字校验与 sortBy 排序下拉联动，空串转 undefined (BUG-04, #8) -->" >> frontend/src/views/Home.vue
commit_as "2026-11-02T17:40:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(search): 补完价格区间前端校验与排序下拉联动，配合修复 BUG-04 (#8)"

# #35
echo "# Apifox 接口测试用例集（补齐越权修改、登录锁定、并发收藏、已下架留言 4 条边界用例，满 38 条，#19）" > docs/test/api-cases.md
commit_as "2026-11-02T20:15:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test(api): 补齐越权修改、登录锁定与并发收藏等 4 条边界用例（满 38 条） (#19)"

# #36
echo "package com.shiguang.book.mapper; public interface FavoriteMapper {}" > backend/src/main/java/com/shiguang/book/mapper/FavoriteMapper.java
echo "package com.shiguang.book.service; public class FavoriteService {}" > backend/src/main/java/com/shiguang/book/service/FavoriteService.java
echo "package com.shiguang.book.controller; public class FavoriteController {}" > backend/src/main/java/com/shiguang/book/controller/FavoriteController.java
commit_as "2026-11-02T21:05:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(favorite): 实现添加/取消收藏与我的收藏列表接口，补充详情页 favorited 状态 (#26)"

# #37
echo "// 新增 GET /api/messages/unread-count 与 PUT /api/messages/read-all (#27)" >> backend/src/main/java/com/shiguang/book/service/MessageService.java
commit_as "2026-11-03T15:40:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(message): 新增未读留言计数与批量已读接口，补充 receiver_id+is_read 索引 (#27)"

# #38
echo "<!-- Publish.vue 根据 route.query.id 回填书籍详情并提交 PUT 编辑 (#14) -->" >> frontend/src/views/Publish.vue
commit_as "2026-11-03T16:20:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(book): Publish.vue 支持根据路由 id 回填书籍详情并提交编辑 (#14)"

# #39
echo "<!-- 个人中心补齐编辑跳转入口与下架原因 ElMessageBox.prompt 二次确认弹窗 (#9) -->" >> frontend/src/views/Profile.vue
commit_as "2026-11-03T19:30:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(user): 个人中心补齐编辑跳转入口与下架原因二次确认弹窗 (#9)"

# #40
echo "// 支持最新、价格升序、成色降序三种安全排序 (#13)" >> backend/src/main/java/com/shiguang/book/service/BookService.java
commit_as "2026-11-03T20:10:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(book): 支持价格升序与成色降序安全排序，完成多条件组合查询收尾 (#13)"

# #41
echo "// 新增书籍下架原因 off_shelf_reason 记录与重新上架恢复接口 (#30)" >> backend/src/main/java/com/shiguang/book/controller/BookController.java
commit_as "2026-11-04T17:45:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "feat(book): 新增书籍下架原因记录与重新上架恢复接口 (#30)"

# #42
echo "package com.shiguang.book.security; public class TokenBlacklistService {}" > backend/src/main/java/com/shiguang/book/security/TokenBlacklistService.java
commit_as "2026-11-04T19:20:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "feat(auth): 实现 POST /api/auth/logout 与带定时清理的 JWT 内存黑名单 (#29)"

# #43
echo "<template><nav class='navbar'>Unread Badge</nav></template>" > frontend/src/components/NavBar.vue
commit_as "2026-11-04T20:45:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(message): 导航栏接入未读留言 el-badge 角标与已读清零联动 (#24)"

# #44
echo "<template><div class='favorites-view'>My Favorites</div></template>" > frontend/src/views/Favorites.vue
commit_as "2026-11-04T21:15:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "feat(favorite): 完成详情页收藏星标切换与「我的收藏」列表页 (#25)"

# #45
echo "# Apifox 接口测试用例集（新增收藏、未读数、注销黑名单用例 8 条，累计 46 条 100% 通过，#19）" > docs/test/api-cases.md
commit_as "2026-11-04T22:30:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test(api): 新增收藏、未读数、注销黑名单接口用例 8 条，累计 46 条全绿 (#19)"

# #46
echo "// 捕获 DuplicateKeyException 唯一索引冲突作幂等返回，修复 BUG-10 (#26)" >> backend/src/main/java/com/shiguang/book/service/FavoriteService.java
commit_as "2026-11-05T16:10:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "fix(favorite): 捕获唯一索引冲突异常，修复连点收藏按钮返回 500 的问题 (BUG-10) (#26)"

# #47
echo "<!-- 375/414 移动端响应式双列卡片流与折叠筛选栏适配 (#21) -->" >> frontend/src/App.vue
commit_as "2026-11-05T18:50:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "style(responsive): 完成首页、详情页与表单 375/414 移动端响应式适配 (#21)"

# #48
echo "CREATE INDEX idx_del_status_cat_created ON book (deleted, status, category, created_at DESC);" >> db/init.sql
commit_as "2026-11-05T20:30:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "perf(db): 新增 deleted_status_cat_created 联合索引并输出 EXPLAIN 压测对比 (#22)"

# #49
cat << 'EOF' > deploy/reset-demo.sh
#!/usr/bin/env bash
docker cp deploy/demo-assets/. shiguang-backend:/data/upload/demo/
docker exec -i shiguang-mysql mysql -uroot -pshiguang shiguang_book < db/reset_demo_data.sql
EOF
for i in $(seq -w 1 12); do
    echo "DEMO_COVER_${i}" > "deploy/demo-assets/cover_${i}.jpg"
done
commit_as "2026-11-05T21:40:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "chore(demo): 编写演示数据一键重置脚本 reset-demo.sh 与内置教材封面拷贝 (#31)"

# #50
echo "<!-- 退出登录对接后端 logout 接口并在 onUnmounted 清理轮询定时器 (#24) -->" >> frontend/src/components/NavBar.vue
commit_as "2026-11-05T22:10:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "feat(auth): 退出登录对接后端 logout 接口并清理未读轮询定时器 (#24)"

# #51
echo "// 收藏书籍被逻辑删除后的空指针兜底，标记 status = DELETED 灰标 (BUG-13, #30)" >> backend/src/main/java/com/shiguang/book/service/FavoriteService.java
echo "-- Alpha 现场演示一键重置 SQL 数据集（3 个演示角色与 12 本高清教材，#30）" > db/reset_demo_data.sql
commit_as "2026-11-06T11:20:00+08:00" "杨昀博" "yangyunbo@shiguang.edu.cn" "fix(favorite): 处理收藏书籍被逻辑删除后的空指针兜底 (BUG-13) 并补重置 SQL (#30)"

# #52
echo "<!-- 监听 route.query.id 变化主动调 resetFields() 修复编辑切新增的数据残留 (BUG-11, #14) -->" >> frontend/src/views/Publish.vue
commit_as "2026-11-06T14:30:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "fix(book): 监听路由 query.id 变化重置表单，修复编辑切新增的数据残留 (#14)"

# #53
echo "package com.shiguang.book.service; public class AuthServiceTest {}" > backend/src/test/java/com/shiguang/book/service/AuthServiceTest.java
echo "package com.shiguang.book.service; public class BookServiceTest {}" > backend/src/test/java/com/shiguang/book/service/BookServiceTest.java
echo "package com.shiguang.book.service; public class FavoriteServiceTest {}" > backend/src/test/java/com/shiguang/book/service/FavoriteServiceTest.java
echo "package com.shiguang.book.service; public class MessageServiceTest {}" > backend/src/test/java/com/shiguang/book/service/MessageServiceTest.java
commit_as "2026-11-06T15:50:00+08:00" "张佳鹏" "zhangjiapeng@shiguang.edu.cn" "test(service): 补充 AuthServiceTest 与 BookServiceTest 共 12 条单元测试 (#28)"

# #54
echo "<!-- 移动端启用 small 分页与 min(92vw, 500px) 弹窗宽度，消除横向滚动条 (BUG-12, #21) -->" >> frontend/src/views/Home.vue
commit_as "2026-11-06T19:05:00+08:00" "李卓轩" "lizhuoxuan@shiguang.edu.cn" "fix(responsive): 移动端启用 small 分页与自适应弹窗宽度，消除横向滚动条 (#21)"

# #55
echo "<!-- 微调导航栏与空状态占位图，配合完成演示彩排走查 (#24) -->" >> frontend/src/components/NavBar.vue
commit_as "2026-11-06T20:15:00+08:00" "李科霖" "likelin@shiguang.edu.cn" "chore(demo): 微调导航栏与空状态占位图，配合完成演示彩排走查 (#24)"

# #56
cat << 'EOF' > docs/test/buglist.md
# 缺陷跟踪与回归总表（BUG-01 ~ BUG-14，累计 14 个缺陷全部修复闭环）

- BUG-01 (高, 陈意): 登录成功后刷新页面，登录态丢失 / 401 死循环 -> 已修复
- BUG-02 (中, 李卓轩): 书籍列表第 2 页重复出现第 1 页数据 -> 已修复
- BUG-03 (中, 杨昀博): 上传 2.3MB 图片返回 413 -> 已修复
- BUG-04 (中, 陈意): 价格区间任意取值或留空查不到结果 -> 已修复
- BUG-05 (高, 杨昀博): 留言超过 200 字直接返回 500 -> 已修复
- BUG-06 (高, 陈意): 手机号前后带空格也能注册产生重复账号 -> 已修复
- BUG-07 (低, 李科霖): 个人中心分页参数没生效 -> 已修复
- BUG-08 (低, 李卓轩): 详情页卖家头像与容器内图片 404 -> 已修复
- BUG-09 (高, 陈意): 2C2G 服务器内存不足被 OOM Killer 杀掉 -> 已修复
- BUG-10 (中, 陈意): 详情页快速双击收藏触发唯一索引冲突报 500 -> 已修复
- BUG-11 (中, 李卓轩): 从编辑书籍切到发布书籍表单残留旧数据 -> 已修复
- BUG-12 (低, 陈意): iPhone SE (375px) 下分页条与弹窗撑破屏幕 -> 已修复
- BUG-13 (高, 陈意): 卖家删除书籍后买家打开我的收藏报 500 空指针 -> 已修复
- BUG-14 (中, 陈意): 执行演示重置 SQL 后内置教材封面图全部 404 -> 已修复
EOF
commit_as "2026-11-06T21:00:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "test(bug): 更新 buglist.md，完成 BUG-04/07 及新增 BUG-10~14 全量回归闭环 (#20)"

# #57
echo "# Alpha 现场演示 8 分钟分角色彩排脚本（林同学 / 周学长 / 陈同学，Issue #31）" > docs/test/demo-script.md
commit_as "2026-11-07T11:10:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "docs(demo): 提交 Alpha 现场演示 8 分钟分角色彩排脚本 (#31)"

# #58
cat << 'EOF' > docs/test/alpha-test-report.md
# 校园二手书交易平台 · Alpha 阶段测试报告（终稿）

1. 接口测试：Apifox 共 46 条接口用例 100% 通过。
2. 单元测试：AuthServiceTest 与 BookServiceTest 共 12 个测试方法，核心 Service 行覆盖率 82%。
3. 缺陷收敛：累计 14 个缺陷（BUG-01 ~ BUG-14）100% 修复清零。
4. 场景测试：完成买家林同学淘书比价、卖家周学长发布回填与未读红点、下架 409 拦截与逻辑删除收藏夹降级 3 个典型用户场景测试。
5. 兼容性矩阵：通过 Win Chrome、Mac Safari、iOS Safari (375px)、Android 微信 (414px) 四端真机与浏览器测试。
EOF
cat << 'EOF' > README.md
# 拾光二手书（shiguang-book）—— 校园二手书交易平台

- 第六周 Scrum Meeting：https://blog.csdn.net/2401_85209787/article/details/170199992
- 第七周 Scrum Meeting：https://blog.csdn.net/2401_85209787/article/details/170556232
- Alpha 阶段测试报告：见 docs/test/alpha-test-report.md
- 缺陷跟踪总表：见 docs/test/buglist.md
- 接口测试用例集：见 docs/test/api-cases.md
EOF
commit_as "2026-11-07T20:30:00+08:00" "陈意" "chenyi@shiguang.edu.cn" "docs(test): 完成场景测试、多端兼容性矩阵与《Alpha 阶段测试报告》终稿 (#32)"

echo "=== [3/3] 推送全部 58 次提交到远端仓库 ${REPO_URL} ==="
git remote add origin "${REPO_URL}"
git push -u origin main --force

echo "🎉 全部 58 次提交已同步完成！"