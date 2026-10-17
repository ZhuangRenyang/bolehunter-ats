# BoleHunter 伯乐猎 —— 后端镜像（多阶段构建）
# 构建：docker build -t bolehunter-backend ./bolehunter-ats

# ---------- 构建阶段：Maven + JDK 17 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先拷 pom 单独下载依赖，利用 Docker 层缓存加速后续构建
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---------- 运行阶段：仅 JRE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# curl 供容器健康检查；docker CLI + compose 插件供后台一键升级（迭代26：经挂载的
# docker.sock 控制宿主机 compose）。Ubuntu 源无独立的 docker-cli 包，用官方静态二进制
ARG DOCKER_CLI_VERSION=27.3.1
ARG DOCKER_COMPOSE_VERSION=2.29.7
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_CLI_VERSION}.tgz" \
        | tar xz --strip-components=1 -C /usr/local/bin docker/docker \
    && curl -fsSL "https://github.com/docker/compose/releases/download/v${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64" \
        -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose \
    && mkdir -p /usr/local/lib/docker/cli-plugins \
    && ln -s /usr/local/bin/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose \
    && docker --version && docker compose version

ENV TZ=Asia/Shanghai \
    JAVA_OPTS=""

COPY --from=build /app/target/bolehunter-ats-*.jar app.jar

EXPOSE 5000
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://127.0.0.1:5000/recruit/health || exit 1

# 迭代28：jar 用绝对路径 /app/app.jar——实测镜像经 GHCR→ACR 同步链路后 WorkingDir
# 配置可能丢失（frontend 不依赖 WorkingDir 故无恙），相对路径会报 Unable to access jarfile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
