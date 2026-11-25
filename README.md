# 拾光二手书（shiguang-book）—— 校园二手书交易平台

基于前后端分离架构的校园二手教材与图书交易平台，支持用户注册登录、书籍发布与编辑、多条件检索排序、封面图片上传、站内留言与未读红点提醒、书籍收藏以及个人中心管理等功能。

## 技术栈
- **后端**：Java 17 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL 8.0 + JJWT 0.11.5 + JUnit 5 + Mockito + JaCoCo 0.8.12
- **前端**：Vue 3 + Vite 5 + Vue Router 4 + Axios + Element Plus
- **部署**：Docker + Docker Compose + Nginx 1.25

---

## 一、快速启动（Docker Compose 一键部署，推荐）

在仓库根目录执行：

```bash
docker compose up -d --build
```

启动完成后：
- **访问地址**：`http://localhost`（Nginx 监听 **80** 端口，反代 `/api/` 至后端 `8080` 端口并托管 `/files/` 静态上传图片）
- **构建门禁**：`backend/Dockerfile` 打包阶段会自动运行全部 JUnit 5 单元测试，全部通过后才会生成后端运行镜像。

### 导入 12 本带封面演示教材（可选）
后端首次启动数据库为空时，会通过 `DataInitializer` 自动写入 3 个账号与 8 本基础演示书；如需加载含封面图的 12 本演示教材数据集，可执行：

```bash
bash deploy/reset-demo.sh
```
> Windows PowerShell 环境下可执行：
> ```powershell
> cmd /c "docker exec -i shiguang-mysql mysql -uroot -proot --default-character-set=utf8mb4 shiguang < db\reset_demo_data.sql"
> docker exec shiguang-backend mkdir -p /app/upload/demo
> docker cp deploy/demo-assets/. shiguang-backend:/app/upload/demo/
> docker exec shiguang-backend chmod -R 755 /app/upload/demo
> ```

---

## 二、本地开发启动指引

1. **初始化数据库**：启动本地 MySQL 8.0，执行 `db/init.sql`（默认库名 `shiguang`，默认账号密码 `root / root`，可通过环境变量 `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` / `MYSQL_USER` / `MYSQL_PWD` 覆盖）。
2. **启动后端**（需 JDK 17 + Maven）：
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   后端监听 `8080` 端口，本地上传图片默认保存至 `./upload`。
3. **启动前端**（需 Node.js 20+）：
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   前端监听 `5173` 端口，Vite 已配置将 `/api` 与 `/files` 代理至 `http://localhost:8080`。

---

## 三、内置演示账号

| 手机号 | 密码 | 说明 |
| :--- | :--- | :--- |
| `13800000001` | `123456` | 演示账号 1（买家 / 卖家） |
| `13800000002` | `123456` | 演示账号 2（买家 / 卖家） |
| `13800000003` | `123456` | 演示账号 3（买家 / 卖家） |



## 四、项目文档导航
- 接口文档：[docs/api.md](docs/api.md)
- 数据库与安全设计：[docs/er-and-security.md](docs/er-and-security.md)
- 前端结构与交互规范：[docs/frontend-spec.md](docs/frontend-spec.md)
- 测试报告：[docs/test/alpha-test-report.md](docs/test/alpha-test-report.md)
- 演示流程指引：[docs/test/demo-script.md](docs/test/demo-script.md)
- 工程实践总结：[docs/se-methods-summary.md](docs/se-methods-summary.md)


