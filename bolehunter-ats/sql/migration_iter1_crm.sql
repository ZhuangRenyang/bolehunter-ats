-- ============================================================================
-- 迭代 1 迁移脚本：客户 CRM（company 语义升级为客户公司 + 客户联系人）
-- ----------------------------------------------------------------------------
-- 适用：bolehunter（MySQL 8.0+），需先执行 migration_iter0_ats.sql
-- 内容：
--   1. company 表增加客户字段：industry / level / client_status / owner_id
--   2. 新建 client_contact 客户联系人表
--   3. 字典补充"客户合作状态"
-- 说明：
--   - company.state 保留原语义（0-未审核 1-通过 2-不通过），老企业认证流程不受影响
--   - 客户建档走新接口 /recruit/client（state 固定为 1，不走老审核流）
--   - 职位已通过 position.company_id 绑定客户，无需变更
-- 注意：本脚本只应执行一次；重复执行会因列/表已存在而报错。
-- ============================================================================

SET NAMES utf8mb4;

USE bolehunter;

-- ----------------------------------------------------------------------------
-- 1. company 表增加客户字段
-- ----------------------------------------------------------------------------
ALTER TABLE `company` ADD COLUMN `industry` varchar(32) DEFAULT NULL COMMENT '所属行业（字典 industry）';
ALTER TABLE `company` ADD COLUMN `level` varchar(32) DEFAULT NULL COMMENT '客户等级（字典 client_level）';
ALTER TABLE `company` ADD COLUMN `client_status` varchar(32) NOT NULL DEFAULT 'potential' COMMENT '客户合作状态（字典 client_status）';
ALTER TABLE `company` ADD COLUMN `owner_id` int(0) DEFAULT NULL COMMENT '负责顾问用户id';

-- 存量数据：默认归为潜在客户，负责人待分配
UPDATE `company` SET `client_status` = 'potential' WHERE `client_status` IS NULL;
CREATE INDEX `idx_company_owner` ON `company` (`owner_id`);

-- ----------------------------------------------------------------------------
-- 2. 客户联系人表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `client_contact`;
CREATE TABLE `client_contact` (
  `id`          int(0) NOT NULL AUTO_INCREMENT COMMENT '联系人id',
  `company_id`  int(0) NOT NULL COMMENT '所属客户公司id',
  `name`        varchar(50) NOT NULL COMMENT '姓名',
  `phone`       varchar(20)  DEFAULT NULL COMMENT '电话',
  `email`       varchar(100) DEFAULT NULL COMMENT '邮箱',
  `title`       varchar(50)  DEFAULT NULL COMMENT '职位',
  `is_primary`  int(0) NOT NULL DEFAULT 0 COMMENT '是否主联系人: 1-是 0-否（同公司仅一个）',
  `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id`   int(0) NOT NULL DEFAULT 1 COMMENT '租户id',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `delete_time` datetime(3)  DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_contact_company` (`company_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '客户联系人表';

-- ----------------------------------------------------------------------------
-- 3. 字典补充：客户合作状态
-- ----------------------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `remark`)
VALUES ('客户合作状态', 'client_status', '客户公司的合作生命周期');

INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_sort`) VALUES
('client_status', '潜在客户', 'potential', 1),
('client_status', '签约合作', 'signing',   2),
('client_status', '暂停合作', 'suspended', 3),
('client_status', '流失',     'lost',      4);
