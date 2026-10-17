# BoleHunter 伯乐猎 —— 后端镜像（多阶段构建）
# 构建：docker build -t bolehunter-backend ./bolehunter-ats

# ---------- 构建阶段：Maven + JDK 17 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先拷 pom 单独下载依赖，利用 Docker 层缓存加速后续构建
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# 迭代28 实测踩坑：曾出现"构建成功但运行时缺 jar"的盲区（GHCR/ACR 镜像均缺 /app/app.jar，
# 而 docker build 验证一直是绿的）。此处显式列出打包产物，让 jar 缺失在构建阶段现形
RUN mvn -B -DskipTests package \
    && ls -la target/*.jar

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
# 构建自证：jar 必须存在且非空（test -s），否则构建阶段直接失败而非运行时崩溃循环
RUN ls -la /app/app.jar && test -s /app/app.jar && echo "app.jar OK ($(du -h /app/app.jar | cut -f1))"

EXPOSE 5000
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://127.0.0.1:5000/recruit/health || exit 1

# 迭代28：jar 用绝对路径 /app/app.jar——实测镜像经 GHCR→ACR 同步链路后 WorkingDir
# 配置可能丢失（frontend 不依赖 WorkingDir 故无恙），相对路径会报 Unable to access jarfile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
