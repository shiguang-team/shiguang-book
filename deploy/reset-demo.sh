#!/usr/bin/env bash
set -e
echo "[reset-demo] 1. 正在重置演示数据库 (3 个演示角色 + 12 本精选教材)..."
docker exec -i shiguang-mysql mysql -uroot -p123456 shiguang_book < db/reset_demo_data.sql
echo "[reset-demo] 2. 正在同步 12 张内置高清教材封面至上传容器卷..."
docker cp deploy/demo-assets/. shiguang-backend:/app/upload/demo/
echo "[reset-demo] 演示环境一键重置完成！"
