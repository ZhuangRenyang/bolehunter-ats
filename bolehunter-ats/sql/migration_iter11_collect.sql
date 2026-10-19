-- ============================================================================
-- 迭代 36 迁移脚本：候选人自助信息收集
-- ----------------------------------------------------------------------------
-- 内容：
--   1. collect_link 收集链接（顾问创建，token 免登录访问，可关闭）
--   2. candidate 表加 audit_status 审核状态列（自助提交的候选人待审核）
-- ============================================================================
SET NAMES utf8mb4;
USE bolehunter;

-- 1. 收集链接
DROP TABLE IF EXISTS `collect_link`;
CREATE TABLE `collect_link` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `token`       varchar(64)  NOT NULL COMMENT '免登录访问令牌',
  `title`       varchar(100) DEFAULT NULL COMMENT '收集用途说明（如：2026 春季人才自荐）',
  `created_by`  int(0)       NOT NULL COMMENT '创建顾问id',
  `status`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：1收集中 0已关闭',
  `expires_at`  datetime(3)  DEFAULT NULL COMMENT '过期时间（空=长期有效）',
  `create_time` datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_collect_token` (`token`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '候选人信息收集链接' ROW_FORMAT = Dynamic;

-- 2. 候选人审核状态（0 待审核 1 已入库；存量数据视为已入库）
ALTER TABLE `candidate` ADD COLUMN `audit_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '审核状态：0待审核 1已入库' AFTER `remark`;
