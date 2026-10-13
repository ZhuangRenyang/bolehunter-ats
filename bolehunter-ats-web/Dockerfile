# BoleHunter 伯乐猎 —— 前端镜像（Vite 构建 + Nginx 托管/反代）
# 构建：docker build -t bolehunter-frontend ./bolehunter-ats-web

# ---------- 构建阶段 ----------
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- 运行阶段 ----------
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
