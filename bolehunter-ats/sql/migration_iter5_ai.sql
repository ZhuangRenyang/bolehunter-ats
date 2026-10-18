-- 迭代19+：AI 应用级配置表（管理员后台填写，环境变量兜底）
-- 动机：不能要求非开发用户配置环境变量；API Key 属敏感凭据，接口回显一律脱敏
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
