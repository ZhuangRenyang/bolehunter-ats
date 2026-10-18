-- ============================================================================
-- 迭代 32 迁移脚本：安全审计双件套
-- ----------------------------------------------------------------------------
-- 内容：
--   1. sys_login_log 登录日志（username/ip/browser/os/结果/失败原因/耗时）
--   2. data_change_log 数据变更日志（表/主键/字段/旧值/新值/操作人）
-- 注意：本脚本只应执行一次；重复执行会因表已存在而报错。
-- ============================================================================
SET NAMES utf8mb4;
USE bolehunter;

-- 1. 登录日志
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username`    varchar(50)  NOT NULL COMMENT '登录用户名',
  `user_id`     int(0)       DEFAULT NULL COMMENT '用户id（用户不存在时为空）',
  `ip`          varchar(64)  DEFAULT NULL COMMENT '登录IP',
  `browser`     varchar(64)  DEFAULT NULL COMMENT '浏览器',
  `os`          varchar(64)  DEFAULT NULL COMMENT '操作系统',
  `result`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '结果：1成功 0失败',
  `fail_reason` varchar(100) DEFAULT NULL COMMENT '失败原因',
  `elapsed_ms`  int(0)       DEFAULT NULL COMMENT '耗时毫秒',
  `create_time` datetime(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_login_username` (`username`) USING BTREE,
  KEY `idx_login_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录日志' ROW_FORMAT = Dynamic;

-- 2. 数据变更日志（字段级）
DROP TABLE IF EXISTS `data_change_log`;
CREATE TABLE `data_change_log` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `table_name`  varchar(64)   NOT NULL COMMENT '数据表名（company/candidate/position）',
  `record_id`   int(0)        NOT NULL COMMENT '记录主键',
  `field`       varchar(64)   NOT NULL COMMENT '变更字段（Java属性名）',
  `old_value`   varchar(1000) DEFAULT NULL COMMENT '旧值',
  `new_value`   varchar(1000) DEFAULT NULL COMMENT '新值',
  `operator_id` int(0)        DEFAULT NULL COMMENT '操作人id',
  `operator`    varchar(50)   DEFAULT NULL COMMENT '操作人用户名',
  `create_time` datetime(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_change` (`table_name`, `record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据变更日志' ROW_FORMAT = Dynamic;
