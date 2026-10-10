<div align="center">

# BoleHunter · 伯乐猎

**千里马常有，而伯乐不常有 —— 让小猎头团队，人人都是伯乐。**

轻量级猎头 **ATS + CRM** · 开源版

</div>

> 为 5–20 人小型猎头团队打造的轻量级 **候选人追踪系统（ATS）+ 客户关系管理（CRM）**。
> 部署零依赖（单机 2C4G 即可），10 分钟能用，Docker Compose 一键起。
>
> **关键词**：招聘软件 / 猎头管理系统 / ATS / 候选人追踪 / recruitment software / headhunter CRM / recruiting / talent acquisition

> 项目溯源：前身为 2022 年校企招聘网毕业设计（v1.0.1，Vue2 + Spring Boot 2 + Lin CMS），2026-10 起全面转型为猎头 ATS 并完成技术栈现代化（详见 `doc/开发记录日志.md`）。

## 定位

**企业是客户，不是用户；HR 是联系人，不是登录账号。** 系统账号只发给猎头团队内部成员（管理员 / 顾问），企业与客户 HR 作为数据对象管理，客户反馈由顾问代录。完整客户 HR 门户、多租户 SaaS、AI 增强属商业版能力（Open Core 模式）。

## 核心功能

- **客户管理**：客户公司建档（行业/等级/合作状态/负责顾问）、联系人（主联系人互斥）、客户维度推荐聚合视图
- **候选人库**：简历导入（PDF/DOC/DOCX + Excel 批量）、规则解析（姓名/电话/邮箱/学历）、手机号+邮箱查重、标签体系、多条件筛选
- **职位推荐**：推荐记录状态机（待推荐 → 已推荐 → 客户已读 → 面试 → Offer → 入职 → 回款，可归档）、拖拽看板、阶段流转历史留痕
- **沟通与任务**：客户沟通逐次代录、跟进任务（今日/逾期提醒）、工作台聚合（统计卡片 + 推荐阶段分布 + 漏斗图）
- **系统底座**：部门/角色权限（RBAC）、数据字典、功能开关、操作日志、多租户预留（单团队默认关闭）

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5 / JDK 17 / MyBatis-Plus 3.5 / MySQL 8 / Redis（可选） |
| 前端 | Vue 3.5 / Element Plus / Vite 5 / Pinia / ECharts |
| 认证 | JWT 双令牌（access + refresh），自研轻量权限层（RBAC 注解扫描） |

## 快速开始

```bash
# 1. 初始化数据库（基线 + 4 个迁移脚本，见 doc/部署指南.md）
mysql -uroot -p < bolehunter-ats/sql/bolehunter.sql
# …按序执行 bolehunter-ats/sql/migration_iter*.sql

# 2. 改数据库密码：bolehunter-ats/src/main/resources/application-dev.yml

# 3. 启动后端
cd bolehunter-ats && mvn compile spring-boot:run -Dspring-boot.run.arguments="--server.port=5000"

# 4. 启动前端
cd bolehunter-ats-web && npm install && npm run dev

# 5. 打开 http://localhost:8081，默认账号 root / 123456
```

完整说明（含 Docker Compose）见 **[doc/部署指南.md](doc/部署指南.md)**；开发过程与踩坑记录见 **[doc/开发记录日志.md](doc/开发记录日志.md)**。

## 目录结构

```
bolehunter-ats/        后端（Spring Boot 3）
  ├─ sql/           基线 SQL + 迭代迁移脚本（按序执行）
  ├─ src/main/java/com/recruit/core/     自研轻量认证/权限层
  └─ src/main/java/com/recruit/...       controller / service / model / extension
bolehunter-ats-web/     前端（Vue 3 + Vite）
doc/                开发记录日志 / 部署指南
recruit-web/        旧版前端（Vue 2，已冻结，仅存量兼容）
```

## 路线图

- [x] 客户 CRM / 候选人库 / 推荐看板 / 沟通与任务 / 工作台
- [ ] 职位管理独立页、推荐漏斗报表增强
- [ ] 免登录推荐确认链接（v1.1，confirm_token 已预留）
- [ ] 简历 AI 解析（`resume_parse` 功能开关已预留）
- [ ] 客户 HR 门户（商业版）

## License

本项目基于开源协议发布，具体以 LICENSE 文件为准。禁止移除版权声明用于闭源分发。
