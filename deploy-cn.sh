#!/usr/bin/env bash
# BoleHunter 伯乐猎 —— 一键部署 · 国内版（迭代28）
#
# 与国际版 deploy.sh 的差异（其余逻辑一致）：
#   1) 未安装 Docker 时自动用国内源安装（apt 系统源 → 腾讯云 docker-ce 源兜底）
#   2) 自动处理当前用户无 Docker 权限问题（加入 docker 组，需重新登录后重跑一次）
#   3) 生成的 .env 将四个镜像全部指向阿里云 ACR 国内源——不碰 ghcr.io / Docker Hub，
#      国内服务器单一镜像源拉取（mysql/redis 也由 CI 同步到 ACR）
#
# 用法：git clone 本仓库后  bash deploy-cn.sh
# 首次运行若触发了安装/加组，按提示重新登录 SSH 后再跑一次即可完成部署。

set -euo pipefail

cd "$(dirname "$0")"

say() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()  { printf '\033[1;32m✓ %s\033[0m\n' "$*"; }
warn(){ printf '\033[1;33m! %s\033[0m\n' "$*"; }
die() { printf '\033[1;31m✗ %s\033[0m\n' "$*" >&2; exit 1; }

# ---------- 0. 镜像源（国内 ACR；换源改 .env 的四个 *_IMAGE 变量即可） ----------
ACR_BASE="crpi-c5nat53ormajywmc.cn-hangzhou.personal.cr.aliyuncs.com/bolehunter"

# ---------- 公网 IP 探测（写入 CORS 白名单，普通用户无需手工编辑） ----------
# 云厂商元数据服务内网直连最快最稳；公网服务兜底；全失败回落 localhost 并提示
detect_public_ip() {
  local ip
  ip=$(curl -s --max-time 2 http://metadata.tencentyun.com/latest/meta-data/public-ipv4 2>/dev/null)
  case "$ip" in *[0-9].*[0-9]) printf '%s' "$ip"; return;; esac
  ip=$(curl -s --max-time 2 http://100.100.100.200/latest/meta-data/eipv4 2>/dev/null)
  case "$ip" in *[0-9].*[0-9]) printf '%s' "$ip"; return;; esac
  for svc in https://api.ipify.org https://ifconfig.me; do
    ip=$(curl -s --max-time 3 "$svc" 2>/dev/null)
    case "$ip" in *[0-9].*[0-9]) printf '%s' "$ip"; return;; esac
  done
  printf ''
}

# ---------- 1. Docker 检查与国内源安装引导 ----------
if ! command -v docker >/dev/null 2>&1; then
  say "未检测到 Docker，尝试用国内源自动安装（Ubuntu/Debian）"
  # shellcheck disable=SC1091
  . /etc/os-release 2>/dev/null || true
  case "${ID:-}" in
    ubuntu|debian)
      sudo apt-get update -y
      # 腾讯云/阿里云机器的系统 apt 源默认已是国内镜像，直装最快
      if sudo apt-get install -y docker.io docker-compose-v2; then
        ok "docker.io + docker-compose-v2 安装完成"
      else
        # 兜底：Docker CE 官方仓库的腾讯云内网镜像
        warn "系统源未找到包，改用腾讯云 docker-ce 源安装"
        sudo install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://mirrors.cloud.tencent.com/docker-ce/linux/ubuntu/gpg \
          | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        CODENAME="$(. /etc/os-release && printf '%s' "$VERSION_CODENAME")"
        echo "deb [signed-by=/etc/apt/keyrings/docker.gpg] https://mirrors.cloud.tencent.com/docker-ce/linux/ubuntu ${CODENAME} stable" \
          | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        sudo apt-get update -y
        sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
      fi
      sudo systemctl enable --now docker
      ;;
    *)
      die "无法自动安装（发行版: ${ID:-未知}）。请手动安装 Docker 后重跑本脚本：https://mirrors.cloud.tencent.com/docker-ce/"
      ;;
  esac
fi

docker compose version >/dev/null 2>&1 \
  || die "缺少 compose 插件：Ubuntu 执行 sudo apt-get install -y docker-compose-v2"

# ---------- 2. daemon 权限自检（deploy.sh 国际版踩过的盲点） ----------
if ! docker ps >/dev/null 2>&1; then
  if [ "$(id -u)" = "0" ]; then
    die "root 用户连接 Docker daemon 失败，请检查 Docker 服务状态：sudo systemctl status docker"
  fi
  sudo usermod -aG docker "$USER" 2>/dev/null || true
  warn "当前用户 $USER 无 Docker 权限，已自动加入 docker 组"
  die "请退出 SSH 重新登录后，再次运行本脚本（仅需一次）"
fi
ok "Docker $(docker --version | awk '{print $3}' | tr -d ,) / compose 就绪（当前用户有 daemon 权限）"

# ---------- 3. 生成 .env（镜像源指向 ACR + 随机密钥 + 自动 CORS 白名单；已存在则复用） ----------
# CORS 白名单自动探测公网 IP（普通用户无需手工编辑）；
# 旧版 .env 里脚本默认生成的 localhost 白名单也会自动升级为公网 IP（手动改过的值不覆盖）
if [ ! -f .env ]; then
  say "生成 .env（镜像走国内 ACR 源 + 随机 DB_PASSWORD/JWT_SECRET + 自动 CORS）"
  rand_hex() {
    if command -v openssl >/dev/null 2>&1; then openssl rand -hex 32
    else head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n'; fi
  }
  PORT_DEFAULT=8080
  PUB_IP=$(detect_public_ip)
  if [ -n "$PUB_IP" ]; then
    CORS_LINE="CORS_ORIGINS=http://${PUB_IP}:${PORT_DEFAULT},http://localhost:${PORT_DEFAULT}"
    ok "探测到公网 IP: ${PUB_IP}（已写入 CORS 白名单）"
  else
    CORS_LINE="CORS_ORIGINS=http://localhost:${PORT_DEFAULT}"
    warn "未能探测公网 IP——从外部访问前请编辑 .env 的 CORS_ORIGINS 加上 http://<服务器IP>:${PORT_DEFAULT}"
  fi
  {
    echo "# 由 deploy-cn.sh 自动生成于 $(date '+%Y-%m-%d %H:%M:%S')（国内版：镜像走阿里云 ACR）"
    echo "DB_PASSWORD=bh_$(rand_hex | cut -c1-16)"
    echo "JWT_SECRET=$(rand_hex)$(rand_hex)"
    echo "$CORS_LINE"
    echo "FRONTEND_PORT=${PORT_DEFAULT}"
    echo "# 镜像源（国内 ACR）；换源改这四行即可"
    echo "BACKEND_IMAGE=${ACR_BASE}/bolehunter-backend:latest"
    echo "FRONTEND_IMAGE=${ACR_BASE}/bolehunter-frontend:latest"
    echo "MYSQL_IMAGE=${ACR_BASE}/mysql:8.4"
    echo "REDIS_IMAGE=${ACR_BASE}/redis:7-alpine"
    echo "WATCHTOWER_IMAGE=${ACR_BASE}/watchtower:latest"
  } > .env
  chmod 600 .env
  ok ".env 已生成（含随机密钥，权限 600，镜像走国内源）"
else
  ok ".env 已存在，沿用现有配置"
  # 升级场景：仍为脚本默认的 localhost 白名单且探测到公网 IP → 自动升级（用户手动改过的值不动）
  if grep -q '^CORS_ORIGINS=http://localhost:8080$' .env 2>/dev/null; then
    PUB_IP=$(detect_public_ip)
    if [ -n "$PUB_IP" ]; then
      sed -i "s|^CORS_ORIGINS=.*|CORS_ORIGINS=http://${PUB_IP}:8080,http://localhost:8080|" .env
      ok "CORS 白名单已自动升级为公网 IP: ${PUB_IP}（backend 将随 up -d 自动重建生效）"
    fi
  fi
fi

# ---------- 4. 拉镜像并启动（全部走 ACR，无需登录） ----------
say "拉取镜像（单一国内源：ACR 公开仓库，免登录）"
docker compose pull || warn "拉取失败（网络受限），尝试使用本地已有镜像继续"

say "启动服务（mysql / redis / backend / frontend）"
docker compose up -d

# ---------- 5. 等待健康（与国际版一致） ----------
say "等待服务健康（首次启动含数据库初始化，可能需要 1-3 分钟）"
PORT=$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2 | tr -d '[:space:]')
PORT=${PORT:-8080}
for i in $(seq 1 60); do
  if curl -fsS "http://127.0.0.1:${PORT}/recruit/health" >/dev/null 2>&1; then
    ok "服务已就绪"
    printf '\n\033[1;32m============================================\n'
    printf '  BoleHunter 部署完成（国内版）\n'
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
