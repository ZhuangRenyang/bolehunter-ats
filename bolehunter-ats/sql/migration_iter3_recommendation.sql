-- ============================================================================
-- 迭代 3 迁移脚本：推荐流程（recommendation + 状态机）
-- ----------------------------------------------------------------------------
-- 适用：bolehunter（MySQL 8.0+），需先执行 migration_iter0/1/2 迁移脚本
-- 内容：
--   1. recommendation 推荐记录表（核心表）
--   2. 新表加入应用层租户拦截名单（代码侧 RecruitTenantLineHandler）
-- 状态机（字典 recommendation_stage，迭代0已预置）：
--   PENDING(待推荐) → RECOMMENDED(已推荐) → CLIENT_READ(客户已读) →
--   INTERVIEW_PENDING(待面试) → INTERVIEWING(面试中) → OFFER_PENDING(待Offer) →
--   OFFERED(Offer) → ONBOARDED(入职) → PAID(回款)，任意阶段可 ARCHIVED(归档)
-- 说明：
--   - client_id 从职位的 company_id 冗余带来，便于按客户聚合查询
--   - confirm_token/confirm_expire 为 v1.1 免登录确认链接预留，本期不使用
--   - (job_id, candidate_id) 活动记录唯一：同一职位同一候选人仅一条进行中的推荐
-- 注意：本脚本只应执行一次。
-- ============================================================================

SET NAMES utf8mb4;

USE bolehunter;

DROP TABLE IF EXISTS `recommendation`;
CREATE TABLE `recommendation` (
  `id`             int(0) NOT NULL AUTO_INCREMENT COMMENT '推荐记录id',
  `job_id`         int(0) NOT NULL COMMENT '职位id（position 表）',
  `candidate_id`   int(0) NOT NULL COMMENT '候选人id（candidate 表）',
  `client_id`      int(0) NOT NULL COMMENT '客户公司id（company 表，自职位冗余）',
  `owner_id`       int(0)       DEFAULT NULL COMMENT '负责顾问用户id',
  `current_stage`  varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '当前阶段（字典 recommendation_stage）',
  `stage_time`     datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '进入当前阶段时间',
  `feedback`       text         DEFAULT NULL COMMENT '客户反馈记录（顾问代录）',
  `feedback_time`  datetime(3)  DEFAULT NULL COMMENT '最近反馈时间',
  `confirm_token`  varchar(64)  DEFAULT NULL COMMENT '免登录确认链接 token（v1.1 预留）',
  `confirm_expire` datetime(3)  DEFAULT NULL COMMENT '确认链接有效期（v1.1 预留）',
  `remark`         varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id`      int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`    datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time`    datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`    datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_rec_job_candidate` (`job_id`, `candidate_id`, `delete_time`) USING BTREE,
  KEY `idx_rec_stage` (`current_stage`) USING BTREE,
  KEY `idx_rec_owner` (`owner_id`) USING BTREE,
  KEY `idx_rec_client` (`client_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '推荐记录表';
