<div align="center">

# BoleHunter ATS

**"There are always thoroughbreds; what's rare is a Bole who can spot them."**
A lightweight **recruitment ATS & CRM for small headhunting teams** (5–20 people).

</div>

> Recruiting software, headhunter CRM, candidate tracking (ATS) — open-source, self-hosted, zero external dependencies (2C4G is enough), up and running in 10 minutes with Docker Compose.

## Why BoleHunter

In Chinese lore, **Bole** is the legendary judge of thoroughbred horses — every recruiter is a Bole in search of talent. BoleHunter packs the complete daily workflow of a headhunting team into one lightweight, self-hosted app.

## Features

- **Client CRM**: client companies (industry/level/engagement status/owner), contacts with primary-contact exclusivity, per-client recommendation views
- **Candidate Pool (ATS)**: resume parsing (PDF/DOC/DOCX + Excel bulk import), phone/email duplicate checks, tags, multi-condition search
- **Recommendation Pipeline**: drag-and-drop kanban across 10 stages (Pending → Recommended → Client Read → Interview → Offer → Onboarded → Paid), stage history timeline
- **Communication & Tasks**: per-recommendation communication logs, follow-up tasks (today/overdue), workbench with stats & funnel chart
- **Admin Base**: RBAC (departments/roles), data dictionaries, feature flags, audit logs, multi-tenant ready

## Tech Stack

Spring Boot 3.5 / JDK 17 / MyBatis-Plus 3.5 / MySQL 8 / Redis (optional) · Vue 3.5 / Element Plus / Vite 5 / Pinia / ECharts · JWT (access + refresh) with a lightweight in-house RBAC layer.

## Quick Start

See [doc/Deployment Guide](doc/部署指南.md) for the full guide (SQL init, config, Docker Compose). Development history and lessons learned: [doc/开发记录日志.md](doc/开发记录日志.md).

## Keywords

recruitment software, ATS, applicant tracking system, headhunter CRM, recruiting, talent acquisition, 招聘软件, 猎头管理系统, 候选人追踪, 伯乐
