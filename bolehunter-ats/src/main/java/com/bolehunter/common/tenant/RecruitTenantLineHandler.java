/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.common.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 租户行级隔离处理器：对登记在 TENANT_TABLES 中的业务表自动追加 tenant_id 条件。
 * 未登记的表（Lin CMS 系统表、聊天表等）不做注入，保证老功能不受影响。
 */
@Slf4j
public class RecruitTenantLineHandler implements TenantLineHandler {

    /**
     * 参与租户隔离的表（与 migration_iter0_ats.sql 中添加 tenant_id 的表保持一致）
     */
    private static final Set<String> TENANT_TABLES = new HashSet<>(Arrays.asList(
            "user", "company", "position", "resume", "application", "interview",
            "category", "notify", "favor", "follow", "hr_company", "education",
            "experience", "project", "sys_dept", "sys_dict_type", "sys_dict_data",
            "feature_flag", "custom_field", "tenant",
            "candidate", "tag", "candidate_tag", "recommendation",
            "communication", "task", "recommendation_stage_history"));

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContext.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return true;
        }
        String name = tableName.replace("`", "").trim().toLowerCase(Locale.ROOT);
        return !TENANT_TABLES.contains(name);
    }
}
