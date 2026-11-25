#!/usr/bin/env bash
set -e
echo "[reset-demo] 1. 正在重置演示数据库 (3 个演示账号 + 12 本教材)..."
docker exec -i shiguang-mysql mysql -uroot -proot --default-character-set=utf8mb4 shiguang < db/reset_demo_data.sql
echo "[reset-demo] 2. 正在同步 12 张演示教材封面至上传容器卷..."
docker exec shiguang-backend mkdir -p /app/upload/demo
docker cp deploy/demo-assets/. shiguang-backend:/app/upload/demo/
docker exec shiguang-backend chmod -R 755 /app/upload/demo
echo "[reset-demo] 演示环境一键重置完成！"