/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.common.tenant;

/**
 * 租户上下文：基于 ThreadLocal 保存当前请求的租户 id。
 * 开源版固定使用默认租户 1；商业版多租户开启后由过滤器从登录用户写入。
 */
public class TenantContext {

    private static final ThreadLocal<Integer> CURRENT = ThreadLocal.withInitial(() -> 1);

    private TenantContext() {
    }

    public static Integer getTenantId() {
        return CURRENT.get();
    }

    public static void setTenantId(Integer tenantId) {
        CURRENT.set(tenantId == null || tenantId <= 0 ? 1 : tenantId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
