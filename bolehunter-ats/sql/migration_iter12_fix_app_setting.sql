-- ============================================================================
-- 迭代 38 修复迁移：补建 app_setting（AI 服务配置表）
-- ----------------------------------------------------------------------------
-- 背景：migration_iter5_ai.sql 里的 `CREATE TABLE IF NOT EXISTS app_setting` 在
--   2026-10-17 首次部署时被"基线对齐"逻辑标记为已执行（baseline=1）但实际未执行，
--   导致生产库缺这张表 —— 系统设置 → AI 服务配置 接口
--   GET /recruit/admin/setting/ai 直接 500（Table 'bolehunter.app_setting' doesn't exist）。
--
-- 处理：以新脚本形式补建（schema_history 中无本文件 → 升级重启时自动执行）。
--   DDL 与 iter5 完全一致，IF NOT EXISTS 保证幂等：已存在的环境重复执行无副作用。
-- ============================================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS app_setting (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    setting_key   VARCHAR(64)  NOT NULL COMMENT '配置键（唯一）',
    setting_value TEXT         NULL COMMENT '配置值（ai_api_key 存明文但接口只回显掩码）',
    create_time   DATETIME(3)  NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_time   DATETIME(3)  NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    delete_time   DATETIME(3)  NULL,
    UNIQUE KEY uk_setting_key (setting_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '应用级配置（键值对，管理员维护）';
