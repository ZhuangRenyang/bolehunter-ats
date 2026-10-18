<div align="center">

# BoleHunter · 伯乐猎

[![CI](https://github.com/ZhuangRenyang/bolehunter-ats/actions/workflows/ci.yml/badge.svg)](https://github.com/ZhuangRenyang/bolehunter-ats/actions/workflows/ci.yml)
![License](https://img.shields.io/badge/License-AGPL--3.0--or--later-blue.svg)

**千里马常有，而伯乐不常有 —— 让小猎头团队，人人都是伯乐。**

轻量级猎头 **ATS + CRM** · 开源版

🖥️ **仓库地址**：[GitHub](https://github.com/ZhuangRenyang/bolehunter-ats) ｜ [Gitee](https://gitee.com/ZhuangRenyang/bolehunter-ats) ｜ 作者：[ZhuangRenyang](https://github.com/ZhuangRenyang)

</div>

> 为 5–20 人小型猎头团队打造的轻量级 **候选人追踪系统（ATS）+ 客户关系管理（CRM）**。
> 部署零依赖（单机 2C4G 即可），10 分钟能用，Docker Compose 一键起。
>
> **关键词**：招聘软件 / 猎头管理系统 / ATS / 候选人追踪 / recruitment software / headhunter CRM / recruiting / talent acquisition

## 定位

**企业是客户，不是用户；HR 是联系人，不是登录账号。** 系统账号只发给猎头团队内部成员（管理员 / 顾问），企业与客户 HR 作为数据对象管理，客户反馈由顾问代录。完整客户 HR 门户、多租户 SaaS、AI 增强属商业版能力（Open Core 模式）。

## 核心功能

- **客户管理**：客户公司建档（行业/等级/合作状态/负责顾问）、联系人（主联系人互斥）、客户维度推荐聚合视图
- **候选人库**：简历导入（PDF/DOC/DOCX + Excel 批量）、**AI 智能解析 + 规则解析双引擎**（开关控制、静默降级）、手机号+邮箱查重、标签体系、多条件筛选
- **职位推荐**：推荐记录状态机（待推荐 → 已推荐 → 客户已读 → 面试 → Offer → 入职 → 回款，可归档）、拖拽看板、阶段流转历史留痕、**免登录客户确认链接**（HR 无需账号即可确认已读/约面/婉拒）
- **AI 能力**（迭代 19-20）：简历结构化抽取（OpenAI 兼容协议，后台可视化配置）、**AI 找人问答**——自然语言搜索候选人，向量语义检索 + 关键词检索双引擎自动降级，LLM 归纳回答
- **沟通与任务**：客户沟通逐次代录、跟进任务（到期站内推送）、**通知中心**（WebSocket 实时到达，铃铛角标即时刷新，断连自动降级轮询）、内部消息单聊（会话列表/历史/未读/已读回执）、工作台聚合（统计卡片 + 推荐漏斗 + **漏斗时间对比 / 顾问业绩排行 / 客户转化率报表 + CSV 导出**）
- **系统底座**：部门/角色权限（RBAC）、数据字典、功能开关、**操作审计**（敏感操作留痕 + 审计流水筛选查询页，删除客户有推荐关联防护与强确认）、**后台一键升级**（拉新镜像重建容器 + 数据库增量自动补齐）、**多语言界面**（中/英，顶栏一键切换、偏好持久化）、多租户预留（单团队默认关闭）、登录失败限流、健康检查
- **平台增强（迭代 29-35）**：珍珠玻璃主题（七层设计令牌/深浅主题/登录神经网络画布/推荐溯源图谱/GSAP 动效）、**安全审计双件套**（登录日志、客户/候选人字段级变更历史）、**数据安全**（手机号/邮箱按角色脱敏、全局系统水印）、**数据库备份与恢复**（一键导出/导入整库）、**企业效率**（推荐单号、页面级帮助文档、意见反馈、版本记录、服务器心跳曲线、文件中心）、菜单激活态与移动端适配优化

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5 / JDK 17 / MyBatis-Plus 3.5 / MySQL 8 / Redis（登录限流） |
| 前端 | Vue 3.5 / Element Plus 按需引入 / Vite 5 / Pinia / ECharts 按需引入 / vue-i18n（中/英） |
| 认证 | JWT 双令牌（access + refresh），自研轻量权限层（RBAC 注解扫描） |
| AI | OpenAI 兼容协议（DeepSeek/通义/Kimi…），chat + embeddings，后台可视化配置 |
| 实时协作 | WebSocket（JWT 握手鉴权）——站内通知即时到达 + 内部单聊 |
| 工程 | GitHub Actions CI（版权头检查/单测/构建/镜像发布 GHCR+阿里云ACR 双源）、Docker Compose、springdoc 接口文档、44 个单测 |

## 快速开始

**Docker 部署（推荐，服务器只需装 Docker）：**

```bash
git clone https://github.com/ZhuangRenyang/bolehunter-ats.git && cd bolehunter-ats
bash deploy.sh            # 国际网络环境：自动生成 .env（随机密钥）→ 拉 GHCR 镜像 → 起服务 → 健康检查
bash deploy-cn.sh         # 国内服务器：自动装 Docker（国内源）→ 镜像全部走阿里云 ACR 国内源 → 起服务
# 打开 http://localhost:8080，默认账号 root / 123456（首次登录立即修改）
```

- **免构建**：默认直接使用 CI 自动发布的公开镜像，服务器无需 JDK/Node/MySQL 任何构建环境
- **国内版（迭代28）**：`deploy-cn.sh` 未装 Docker 时自动用国内源安装；四个镜像（含 mysql/redis）全部由 CI 同步到阿里云 ACR，单一国内源拉取，不碰 ghcr.io / Docker Hub
- **自动运行**：全部容器带 `restart: unless-stopped`，服务器重启或进程崩溃自动拉起
- 手动等价：`cp .env.example .env && docker compose up -d`；本地源码构建见 `docker-compose.build.yml`

**本地开发：**

```bash
# 后端（先按 doc/部署指南.md 初始化数据库）
cd bolehunter-ats && mvn compile spring-boot:run -Dspring-boot.run.arguments="--server.port=5000"
# 前端
cd bolehunter-ats-web && npm install && npm run dev   # http://localhost:8081
```

**AI 能力启用**（可选）：管理员登录 → 设置 → AI 服务配置 → 填入 OpenAI 兼容服务的 Key（DeepSeek 注册即送额度）→ 功能开关打开 `resume_parse` / `ai_ask`。

完整说明见 **[doc/部署指南.md](doc/部署指南.md)**；开发过程与踩坑记录见 **[doc/开发记录日志.md](doc/开发记录日志.md)**。

## 目录结构

```
bolehunter-ats/        后端（Spring Boot 3）
  ├─ sql/           基线 SQL + 迭代迁移脚本（按序执行，Compose 自动初始化）
  ├─ src/main/java/com/bolehunter/core/       自研轻量认证/权限层
  ├─ src/main/java/com/bolehunter/module/     业务模块（candidate/recommendation/…）
  └─ src/main/java/com/bolehunter/extension/  扩展能力（简历解析 / AI 客户端）
bolehunter-ats-web/     前端（Vue 3 + Vite）
.github/workflows/     CI（版权头检查 / 单测 / 构建 / GHCR 镜像发布）
doc/                部署指南 / 开发记录日志 / 路线图
scripts/            License 版权头合规检查脚本
```

## 路线图

- [x] 客户 CRM / 候选人库 / 推荐看板 / 沟通与任务 / 工作台 / 职位管理页
- [x] 任务到期推送 + 通知中心 + 看板角标；数据看板（漏斗对比 / 业绩排行 / 转化率）
- [x] 免登录推荐确认链接；AI 简历解析 + AI 找人问答（RAG）；安全加固；Docker Compose；CI/CD（GHCR）
- [x] 操作审计增强；WebSocket 实时协作；部署零门槛；后台一键升级；国际化骨架（中/英）
- [ ] 客户 HR 门户（商业版）

## 开源协议与版权

本项目以 **GNU AGPL-3.0-or-later**（v3 或更高版本）协议开源（全文见 [LICENSE](LICENSE)，署名信息见 [NOTICE.txt](NOTICE.txt)）。

这意味着：

- ✅ 你可以自由使用、学习、修改、分发本项目，包括内部商用
- ⚠️ **以网络服务形式（含 SaaS 托管）向第三方提供本产品或其修改版，必须依据 AGPL-3.0 第 13 条向你的服务使用者开源全部修改代码** —— 简单说：拿本项目做 SaaS 卖钱，你的代码也必须开源
- ⚠️ 复制/分发时**必须保留** LICENSE、NOTICE.txt 及源文件头部的版权与来源声明（含作者 GitHub / Gitee 双仓库链接）
- ⚠️ "BoleHunter"、"伯乐猎" 名称与视觉标识为本项目保留标识，未经作者书面许可，派生项目不得使用相同或近似名称推广

**商业授权**：如需**闭源商业化 / SaaS 部署 / 解除 AGPL 传染义务**，可通过作者 GitHub 或 Gitee 账号（[ZhuangRenyang](https://github.com/ZhuangRenyang)）私信获取商业许可。

© 2026 ZhuangRenyang · BoleHunter 伯乐猎 · 保留所有权利
