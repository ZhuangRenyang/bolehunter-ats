-- ============================================================================
-- 迭代 34 迁移脚本：体验细节包
-- ----------------------------------------------------------------------------
-- 内容：
--   1. sys_user_preference 用户偏好（表格自定义列等用户级配置持久化）
--   2. feedback 意见反馈（登录用户提交，管理员查看处理）
-- ============================================================================
SET NAMES utf8mb4;
USE bolehunter;

-- 1. 用户偏好（user_id + pref_key 唯一，pref_value 为 JSON 字符串）
DROP TABLE IF EXISTS `sys_user_preference`;
CREATE TABLE `sys_user_preference` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id`     int(0)       NOT NULL COMMENT '用户id',
  `pref_key`    varchar(64)  NOT NULL COMMENT '偏好键（如 table_columns:/client/list）',
  `pref_value`  text         DEFAULT NULL COMMENT '偏好值（JSON）',
  `create_time` datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_pref` (`user_id`, `pref_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户偏好' ROW_FORMAT = Dynamic;

-- 2. 意见反馈
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id`     int(0)       DEFAULT NULL COMMENT '提交人id',
  `username`    varchar(50)  DEFAULT NULL COMMENT '提交人',
  `content`     varchar(2000) NOT NULL COMMENT '反馈内容',
  `status`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已处理',
  `reply`       varchar(1000) DEFAULT NULL COMMENT '处理回复',
  `create_time` datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_feedback_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '意见反馈' ROW_FORMAT = Dynamic;
