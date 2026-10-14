#!/usr/bin/env bash
# BoleHunter 伯乐猎 —— 一键部署脚本（迭代25）
# 用法：bash deploy.sh
# 做四件事：检查 Docker → 自动生成 .env（随机密钥）→ 拉取镜像并启动 → 等待健康后打印访问信息
# 前置条件仅一个：服务器已安装 Docker（含 compose 插件）。数据卷与容器带 restart: unless-stopped，
# 服务器重启或进程崩溃都会自动拉起，无需人工干预。

set -euo pipefail

cd "$(dirname "$0")"

say() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()  { printf '\033[1;32m✓ %s\033[0m\n' "$*"; }
warn(){ printf '\033[1;33m! %s\033[0m\n' "$*"; }
die() { printf '\033[1;31m✗ %s\033[0m\n' "$*" >&2; exit 1; }

# ---------- 1. 环境检查 ----------
say "检查 Docker 环境"
command -v docker >/dev/null 2>&1 || die "未找到 docker，请先安装：https://docs.docker.com/engine/install/"
docker compose version >/dev/null 2>&1 || die "缺少 compose 插件，请安装 docker-compose-plugin"
ok "Docker $(docker --version | awk '{print $3}' | tr -d ,) / compose 就绪"

# ---------- 2. 生成 .env（已存在则复用，不覆盖） ----------
if [ ! -f .env ]; then
  say "生成 .env（随机 DB_PASSWORD 与 JWT_SECRET）"
  rand_hex() {
    if command -v openssl >/dev/null 2>&1; then openssl rand -hex 32
    else head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n'; fi
  }
  {
    echo "# 由 deploy.sh 自动生成于 $(date '+%Y-%m-%d %H:%M:%S')"
    echo "DB_PASSWORD=bh_$(rand_hex | cut -c1-16)"
    echo "JWT_SECRET=$(rand_hex)$(rand_hex)"
    echo "CORS_ORIGINS=http://localhost:8080"
    echo "FRONTEND_PORT=8080"
  } > .env
  chmod 600 .env
  ok ".env 已生成（含随机密钥，权限 600）"
else
  ok ".env 已存在，沿用现有配置"
fi

# ---------- 3. 拉镜像并启动 ----------
say "拉取最新镜像（GHCR 公开镜像，无需登录）"
docker compose pull || warn "拉取失败（可能网络受限），尝试使用本地已有镜像继续"

say "启动服务（mysql / redis / backend / frontend）"
docker compose up -d

# ---------- 4. 等待健康 ----------
say "等待服务健康（首次启动含数据库初始化，可能需要 1-3 分钟）"
PORT=$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2 | tr -d '[:space:]')
PORT=${PORT:-8080}
for i in $(seq 1 60); do
  if curl -fsS "http://127.0.0.1:${PORT}/recruit/health" >/dev/null 2>&1; then
    ok "服务已就绪"
    printf '\n\033[1;32m============================================\n'
    printf '  BoleHunter 部署完成\n'
    printf '  访问:   http://<服务器IP>:%s\n' "$PORT"
    printf '  账号:   root / 123456（首次登录立即修改）\n'
    printf '  常用:   docker compose logs -f backend\n'
    printf '          docker compose ps\n'
    printf '============================================\033[0m\n'
    exit 0
  fi
  sleep 5
done

warn "超时未探活成功，查看日志定位问题："
docker compose ps || true
docker compose logs --tail 50 backend || true
exit 1
