#!/bin/bash
# 迭代 28/29 修复：执行被基线对齐跳过的 iter6/iter7 迁移并记账
set -e
cd ~/bolehunter-ats

echo "== 1. 执行迁移 iter6（AI 列）与 iter7（IM 表）=="
for f in migration_iter6_ai_ask.sql migration_iter7_ws.sql; do
  docker exec -i bolehunter-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --force bolehunter' < bolehunter-ats/sql/$f
  echo "✓ $f 已执行"
done

echo "== 2. schema_history 记账（防止后续重复执行）=="
docker exec bolehunter-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "INSERT IGNORE INTO schema_history (filename, success, baseline, executed_at, error_msg) VALUES (\"migration_iter6_ai_ask.sql\",1,0,CURRENT_TIMESTAMP(3),NULL),(\"migration_iter7_ws.sql\",1,0,CURRENT_TIMESTAMP(3),NULL);" bolehunter'
echo "✓ 已记账"

echo "== 3. 表结构验证 =="
docker exec bolehunter-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SHOW TABLES LIKE \"im_message\"; SHOW COLUMNS FROM candidate LIKE \"resume_text\"; SELECT flag_key, enabled FROM feature_flag WHERE flag_key IN (\"im_realtime\",\"ai_ask\");" bolehunter'

echo "== 4. 重启 backend 刷新连接池 =="
docker compose restart backend
echo "等待就绪（约 35 秒）..."
sleep 35

echo "== 5. 接口验证 =="
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/recruit/user/login -H 'Content-Type: application/json' -d '{"username":"root","password":"123456"}' | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  echo "⚠️ 登录失败（root 密码可能已修改），请在浏览器验证 im/unread 接口"
else
  echo "im/unread: $(curl -s http://127.0.0.1:8080/recruit/im/unread -H "Authorization: Bearer $TOKEN")"
  echo "notify/all: $(curl -s "http://127.0.0.1:8080/recruit/notify/all?userName=root" -H "Authorization: Bearer $TOKEN" | head -c 150)"
  echo "upgrade: $(curl -s http://127.0.0.1:8080/recruit/admin/upgrade/status -H "Authorization: Bearer $TOKEN")"
fi
echo "== 修复完成 =="
