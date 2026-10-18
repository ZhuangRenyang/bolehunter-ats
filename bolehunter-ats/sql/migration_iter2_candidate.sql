-- ============================================================================
-- 迭代 2 迁移脚本：候选人库（candidate + 标签体系）
-- ----------------------------------------------------------------------------
-- 适用：bolehunter（MySQL 8.0+），需先执行 migration_iter0/1 迁移脚本
-- 内容：
--   1. candidate 候选人表（姓名/电话/邮箱/当前公司/当前职位/学历/城市/来源/负责人/简历文件/ext_json）
--   2. tag 标签表 + candidate_tag 候选人标签关联表
--   3. 字典补充：学历 education
-- 设计要点：
--   - 查重口径：同租户内 phone 或 email 唯一（应用层校验，重复抛 80180）
--   - 简历文件存本地 assets/resume/，candidate.resume_file 存相对路径
--   - ext_json 预留自定义字段（迭代4接 custom_field 定义表）
-- 注意：本脚本只应执行一次。
-- ============================================================================

SET NAMES utf8mb4;

USE bolehunter;

-- ----------------------------------------------------------------------------
-- 1. 候选人表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate` (
  `id`               int(0) NOT NULL AUTO_INCREMENT COMMENT '候选人id',
  `name`             varchar(50)  NOT NULL COMMENT '姓名',
  `phone`            varchar(20)  DEFAULT NULL COMMENT '手机号（查重键之一）',
  `email`            varchar(100) DEFAULT NULL COMMENT '邮箱（查重键之一）',
  `current_company`  varchar(100) DEFAULT NULL COMMENT '当前公司',
  `current_position` varchar(100) DEFAULT NULL COMMENT '当前职位',
  `education`        varchar(32)  DEFAULT NULL COMMENT '学历（字典 education）',
  `city`             varchar(50)  DEFAULT NULL COMMENT '所在城市',
  `source`           varchar(32)  NOT NULL DEFAULT 'sourcing' COMMENT '来源（字典 candidate_source）',
  `owner_id`         int(0)       DEFAULT NULL COMMENT '负责顾问用户id',
  `resume_file`      varchar(255) DEFAULT NULL COMMENT '原始简历文件相对路径（assets 下）',
  `ext_json`         json         DEFAULT NULL COMMENT '自定义字段扩展数据',
  `remark`           varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id`        int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`      datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time`      datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`      datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_candidate_phone` (`phone`) USING BTREE,
  KEY `idx_candidate_email` (`email`) USING BTREE,
  KEY `idx_candidate_owner` (`owner_id`) USING BTREE,
  KEY `idx_candidate_name` (`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '候选人表';

-- ----------------------------------------------------------------------------
-- 2. 标签表（同租户内名称唯一；软删后允许重建同名标签）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '标签id',
  `name`        varchar(50) NOT NULL COMMENT '标签名',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tag_name` (`name`, `tenant_id`, `delete_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '标签表';

-- ----------------------------------------------------------------------------
-- 3. 候选人标签关联表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `candidate_tag`;
CREATE TABLE `candidate_tag` (
  `id`           int(0) NOT NULL AUTO_INCREMENT COMMENT '关联id',
  `candidate_id` int(0) NOT NULL COMMENT '候选人id',
  `tag_id`       int(0) NOT NULL COMMENT '标签id',
  `tenant_id`    int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`  datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ct_candidate` (`candidate_id`) USING BTREE,
  KEY `idx_ct_tag` (`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '候选人标签关联表';

-- ----------------------------------------------------------------------------
-- 4. 字典补充：学历
-- ----------------------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `remark`)
VALUES ('学历', 'education', '候选人最高学历');

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_sort`) VALUES
('education', '大专', 'associate', 1),
('education', '本科', 'bachelor',  2),
('education', '硕士', 'master',   3),
('education', '博士', 'doctor',   4),
('education', '其他', 'other',    9);
