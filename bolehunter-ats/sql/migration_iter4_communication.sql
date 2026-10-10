-- ============================================================================
-- 迭代 4 迁移脚本：沟通记录 + 任务提醒 + 推荐阶段流转历史
-- ----------------------------------------------------------------------------
-- 适用：bolehunter（MySQL 8.0+），需先执行 migration_iter0/1/2/3 迁移脚本
-- 内容：
--   1. communication 沟通记录表（挂推荐记录，顾问逐次代录与客户 HR 的沟通）
--   2. task 任务表（到期提醒；v1.0 用工作台聚合查询，推送能力后续迭代接）
--   3. recommendation_stage_history 推荐阶段流转历史（append-only 留痕）
-- 注意：本脚本只应执行一次。
-- ============================================================================

USE bolehunter;

-- ----------------------------------------------------------------------------
-- 1. 沟通记录表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `communication`;
CREATE TABLE `communication` (
  `id`              int(0) NOT NULL AUTO_INCREMENT COMMENT '沟通记录id',
  `recommendation_id` int(0) NOT NULL COMMENT '推荐记录id',
  `type`            varchar(32) NOT NULL DEFAULT 'phone' COMMENT '沟通方式（字典 communication_type）',
  `content`         varchar(1000) NOT NULL COMMENT '沟通内容',
  `communicator_id` int(0)       DEFAULT NULL COMMENT '记录人用户id',
  `communicate_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '沟通时间',
  `tenant_id`       int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`     datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time`     datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`     datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_comm_recommendation` (`recommendation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '沟通记录表';

-- ----------------------------------------------------------------------------
-- 2. 任务表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `title`       varchar(100) NOT NULL COMMENT '任务标题',
  `content`     varchar(500) DEFAULT NULL COMMENT '任务说明',
  `due_time`    datetime(3) NOT NULL COMMENT '到期时间',
  `owner_id`    int(0)       DEFAULT NULL COMMENT '负责顾问用户id',
  `related_type` varchar(32) DEFAULT NULL COMMENT '关联对象类型（recommendation/client/candidate）',
  `related_id`  int(0)       DEFAULT NULL COMMENT '关联对象id',
  `status`      tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态：0待办 1完成',
  `done_time`   datetime(3)  DEFAULT NULL COMMENT '完成时间',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_owner_status` (`owner_id`, `status`) USING BTREE,
  KEY `idx_task_due` (`due_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '任务表';

-- ----------------------------------------------------------------------------
-- 3. 推荐阶段流转历史表（append-only，只增不改不删）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `recommendation_stage_history`;
CREATE TABLE `recommendation_stage_history` (
  `id`               int(0) NOT NULL AUTO_INCREMENT COMMENT '历史id',
  `recommendation_id` int(0) NOT NULL COMMENT '推荐记录id',
  `from_stage`       varchar(32)  DEFAULT NULL COMMENT '原阶段（首次创建为 NULL）',
  `to_stage`         varchar(32) NOT NULL COMMENT '新阶段',
  `feedback`         varchar(1000) DEFAULT NULL COMMENT '流转时代录的客户反馈',
  `operator_id`      int(0)       DEFAULT NULL COMMENT '操作人用户id',
  `tenant_id`        int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`      datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  `update_time`      datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`      datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_rsh_recommendation` (`recommendation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '推荐阶段流转历史表';

-- ----------------------------------------------------------------------------
-- 4. 字典补充：沟通方式
-- ----------------------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `remark`)
VALUES ('沟通方式', 'communication_type', '顾问与客户沟通渠道');

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_sort`) VALUES
('communication_type', '电话', 'phone', 1),
('communication_type', '微信', 'wechat', 2),
('communication_type', '邮件', 'email', 3),
('communication_type', '面谈', 'meeting', 4),
('communication_type', '其他', 'other', 9);
