-- ============================================================================
-- 迭代 0 迁移脚本：开源版 ATS + CRM 基础改造
-- ----------------------------------------------------------------------------
-- 适用：bolehunter（MySQL 8.0+）
-- 内容：
--   1. tenant 租户表（预留多租户，开源版固定使用租户 1）
--   2. 核心业务表统一加 tenant_id（默认 1，兼容老数据）
--   3. user 表加 user_type（ADMIN/CONSULTANT/CLIENT_HR/READONLY）与 dept_id
--   4. sys_dept 部门表（RuoYi 式树形，极简组织架构）
--   5. sys_dict_type / sys_dict_data 字典表 + 初始业务字典
--   6. feature_flag 功能开关表 + 初始开关
--   7. custom_field 自定义字段定义表 + 核心业务表 ext_json 预留列
--   8. 内置角色数据（复用 Lin CMS group 体系）
-- 注意：本脚本只应执行一次；重复执行会因列/表已存在而报错。
-- 老功能兼容：tenant_id 均带 DEFAULT 1，应用层租户拦截器默认关闭
-- （application.yml -> tenant.enable=false），开启后自动隔离。
-- ============================================================================

SET NAMES utf8mb4;

USE bolehunter;

-- ----------------------------------------------------------------------------
-- 1. 租户表（预留）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '租户id',
  `name`        varchar(100) NOT NULL COMMENT '租户名称',
  `contact`     varchar(50)  DEFAULT NULL COMMENT '联系人',
  `phone`       varchar(20)  DEFAULT NULL COMMENT '联系电话',
  `status`      int(0) NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
  `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '租户表';

INSERT INTO `tenant` (`id`, `name`, `contact`, `status`, `remark`)
VALUES (1, '默认团队', '管理员', 1, '开源版默认租户');

-- ----------------------------------------------------------------------------
-- 2. 核心业务表统一加 tenant_id（默认 1，兼容老数据）
-- ----------------------------------------------------------------------------
ALTER TABLE `user`        ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `company`     ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `position`    ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `resume`      ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `application` ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `interview`   ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `category`    ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `notify`      ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `favor`       ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `follow`      ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `hr_company`  ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `education`   ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `experience`  ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';
ALTER TABLE `project`     ADD COLUMN `tenant_id` int(0) NOT NULL DEFAULT 1 COMMENT '租户id';

-- ----------------------------------------------------------------------------
-- 3. user 表加用户类型与部门
-- ----------------------------------------------------------------------------
ALTER TABLE `user` ADD COLUMN `user_type` varchar(20) NOT NULL DEFAULT 'CONSULTANT'
  COMMENT '用户类型: ADMIN-管理员 CONSULTANT-顾问 CLIENT_HR-客户HR READONLY-只读';
ALTER TABLE `user` ADD COLUMN `dept_id` int(0) DEFAULT NULL COMMENT '部门id';

-- 按现有分组语义初始化 user_type：
--   分组 4 admin(平台管理员) / 5 sysadmin(系统管理员) -> ADMIN
--   分组 3 hr(招聘者) -> CLIENT_HR（后续迭代改造为客户门户角色）
--   其余（含 root）保持默认；root 为超级管理员
UPDATE `user` u
  JOIN `user_group` ug ON ug.`user_id` = u.`id`
   SET u.`user_type` = 'ADMIN'
 WHERE ug.`group_id` IN (4, 5);
UPDATE `user` u
  JOIN `user_group` ug ON ug.`user_id` = u.`id`
   SET u.`user_type` = 'CLIENT_HR'
 WHERE ug.`group_id` = 3;
UPDATE `user` SET `user_type` = 'ADMIN' WHERE `username` = 'root';

-- ----------------------------------------------------------------------------
-- 4. 部门表（树形，极简组织架构）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id`   int(0) NOT NULL DEFAULT 0 COMMENT '父部门id，0为顶级',
  `dept_name`   varchar(50) NOT NULL COMMENT '部门名称',
  `sort`        int(0) NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `leader`      varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone`       varchar(20) DEFAULT NULL COMMENT '联系电话',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_dept_parent` (`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '部门表';

INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `sort`, `leader`) VALUES
(1, 0, '总部',   0, NULL),
(2, 1, '猎头一部', 1, NULL),
(3, 1, '猎头二部', 2, NULL),
(4, 1, '客户成功部', 3, NULL);

-- ----------------------------------------------------------------------------
-- 5. 字典表（RuoYi 式：类型 + 数据）
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '字典类型id',
  `dict_name`   varchar(64) NOT NULL COMMENT '字典名称',
  `dict_type`   varchar(64) NOT NULL COMMENT '字典类型键（唯一）',
  `status`      int(0) NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
  `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_type` (`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '字典类型表';

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '字典数据id',
  `dict_type`   varchar(64) NOT NULL COMMENT '所属字典类型键',
  `dict_label`  varchar(64) NOT NULL COMMENT '显示标签',
  `dict_value`  varchar(64) NOT NULL COMMENT '存储值',
  `dict_sort`   int(0) NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status`      int(0) NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-停用',
  `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_dict_data_type` (`dict_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '字典数据表';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `remark`) VALUES
('客户等级', 'client_level', '客户公司分级'),
('所属行业', 'industry',     '客户/候选人行业'),
('推荐阶段', 'recommendation_stage', '推荐流程状态机（固定核心流程）'),
('候选人来源', 'candidate_source', '简历来源渠道'),
('职位状态', 'position_status', '职位生命周期状态'),
('沟通方式', 'communication_type', '沟通记录渠道');

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_sort`) VALUES
('client_level', 'A级-重点客户', 'A', 1),
('client_level', 'B级-普通客户', 'B', 2),
('client_level', 'C级-潜在客户', 'C', 3),
('industry', 'IT/互联网/游戏', 'it',        1),
('industry', '医疗健康',       'medical',   2),
('industry', '智能制造',       'manufacture', 3),
('industry', '金融',           'finance',   4),
('industry', '教育',           'education', 5),
('industry', '出海/跨境电商',   'export',    6),
('industry', '其他',           'other',     99),
('recommendation_stage', '待推荐',   'PENDING',      1),
('recommendation_stage', '已推荐',   'RECOMMENDED',  2),
('recommendation_stage', '客户已读', 'CLIENT_READ',  3),
('recommendation_stage', '待面试',   'INTERVIEW_PENDING', 4),
('recommendation_stage', '面试中',   'INTERVIEWING', 5),
('recommendation_stage', '待Offer',  'OFFER_PENDING', 6),
('recommendation_stage', 'Offer',    'OFFERED',      7),
('recommendation_stage', '入职',     'ONBOARDED',    8),
('recommendation_stage', '回款',     'PAID',         9),
('recommendation_stage', '归档',     'ARCHIVED',     10),
('candidate_source', '主动投递',   'apply',    1),
('candidate_source', '猎头寻访',   'sourcing', 2),
('candidate_source', '内部推荐',   'referral', 3),
('candidate_source', '简历库激活', 'reactive', 4),
('position_status', '招聘中', 'OPEN',    1),
('position_status', '暂停',   'PAUSED',  2),
('position_status', '完成',   'DONE',    3),
('position_status', '关闭',   'CLOSED',  4),
('communication_type', '电话', 'phone', 1),
('communication_type', '微信', 'wechat', 2),
('communication_type', '邮件', 'email', 3),
('communication_type', '面谈', 'meeting', 4),
('communication_type', '备注', 'note', 5);

-- ----------------------------------------------------------------------------
-- 6. 功能开关表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `feature_flag`;
CREATE TABLE `feature_flag` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '开关id',
  `flag_key`    varchar(64) NOT NULL COMMENT '开关键（唯一）',
  `flag_name`   varchar(64) NOT NULL COMMENT '开关名称',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `enabled`     int(0) NOT NULL DEFAULT 1 COMMENT '是否开启: 1-开 0-关',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_flag_key` (`flag_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '功能开关表';

INSERT INTO `feature_flag` (`flag_key`, `flag_name`, `description`, `enabled`) VALUES
('resume_parse',    '简历解析',   '导入简历后自动解析姓名/电话/邮箱等字段（迭代2）', 0),
('client_portal',   '客户门户',   '客户HR只读门户入口（简易版）', 0),
('ai_assist',       'AI提效',     'AI简历摘要/人岗匹配/话术生成（迭代7）', 0),
('notify_mail',     '邮件通知',   '任务到期与推荐反馈邮件提醒', 0),
('notify_wecom',    '企微通知',   '企业微信机器人通知', 0),
('data_export',     '数据导出',   '候选人/推荐记录导出 Excel', 1),
('candidate_dedup', '候选人查重', '手机号+邮箱组合查重提示', 1);

-- ----------------------------------------------------------------------------
-- 7. 自定义字段（固定业务表 + JSON 扩展列，不做万能表单）
--    边界：自定义字段不参与核心流程与状态机；每对象建议 <= 20 个
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `custom_field`;
CREATE TABLE `custom_field` (
  `id`           int(0) NOT NULL AUTO_INCREMENT COMMENT '字段id',
  `entity_type`  varchar(32) NOT NULL COMMENT '业务对象: CANDIDATE/CLIENT/JOB',
  `field_key`    varchar(64) NOT NULL COMMENT '字段键（JSON内键名）',
  `field_label`  varchar(64) NOT NULL COMMENT '字段显示名',
  `field_type`   varchar(32) NOT NULL DEFAULT 'text' COMMENT '类型: text/number/date/select/multiselect',
  `options_json` text        DEFAULT NULL COMMENT 'select/multiselect 选项JSON数组',
  `required`     int(0) NOT NULL DEFAULT 0 COMMENT '是否必填: 1-是 0-否',
  `sort`         int(0) NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `enabled`      int(0) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `tenant_id`    int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time`  datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_field_entity_key` (`entity_type`, `field_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '自定义字段定义表';

-- 核心业务对象预留 JSON 扩展列（company->CLIENT, position->JOB, resume->CANDIDATE）
ALTER TABLE `company`  ADD COLUMN `ext_json` json DEFAULT NULL COMMENT '自定义字段扩展数据';
ALTER TABLE `position` ADD COLUMN `ext_json` json DEFAULT NULL COMMENT '自定义字段扩展数据';
ALTER TABLE `resume`   ADD COLUMN `ext_json` json DEFAULT NULL COMMENT '自定义字段扩展数据';

-- ----------------------------------------------------------------------------
-- 8. 内置角色（复用 Lin CMS group 体系，权限在后台"角色管理"中分配）
--    现有分组：1-root 2-JobSeeker 3-hr 4-admin 5-sysadmin
-- ----------------------------------------------------------------------------
INSERT INTO `group` (`id`, `name`, `info`, `level`, `create_time`, `update_time`)
VALUES (6, '顾问(Consultant)', '猎头顾问：管理本人客户/职位/候选人数据', 3, NOW(), NOW()),
       (7, '只读(Readonly)',   '只读角色：仅查看，不可修改',            3, NOW(), NOW());
