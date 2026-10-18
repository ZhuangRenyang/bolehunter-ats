-- ============================================================================
-- 迭代 33 迁移脚本：数据安全与治理
-- ----------------------------------------------------------------------------
-- 内容：
--   1. feature_flag 新增 data_masking（数据脱敏）与 sys_watermark（系统水印）开关
--   2. recommendation 表加 order_no 推荐单号列 + 回填历史数据
-- ============================================================================
SET NAMES utf8mb4;
USE bolehunter;

-- 1. 功能开关（默认关闭，管理员在系统设置-功能开关中开启）
INSERT INTO feature_flag (flag_key, flag_name, description, enabled)
VALUES
('data_masking', '数据脱敏', '手机号/邮箱在列表展示时脱敏（管理员始终可见全量）', 0),
('sys_watermark', '系统水印', '全页面显示当前用户与时间的水印，防截图外泄', 0)
ON DUPLICATE KEY UPDATE flag_name = VALUES(flag_name);

-- 2. 推荐单号（REC-yyyyMMdd-序号，序号取记录 id 保证唯一）
ALTER TABLE `recommendation` ADD COLUMN `order_no` varchar(32) DEFAULT NULL COMMENT '推荐单号' AFTER `id`;
UPDATE `recommendation` SET `order_no` = CONCAT('REC-', DATE_FORMAT(create_time, '%Y%m%d'), '-', LPAD(id, 4, '0')) WHERE `order_no` IS NULL;
CREATE UNIQUE INDEX `uk_order_no` ON `recommendation` (`order_no`);
