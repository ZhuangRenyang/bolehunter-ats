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
package com.bolehunter.core.audit;

import com.bolehunter.model.DataChangeLogDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.DataChangeLogService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 数据变更记录器（迭代 32 安全审计）
 *
 * <p>反射对比更新前后的 DO 对象，把发生变化的字段逐条写入 data_change_log。
 * 在 Service/Controller 的更新方法内显式调用：先快照 before，执行更新后以
 * 更新后的对象作为 after 传入。
 *
 * <p>排除字段：主键、时间戳、租户（技术列不参与业务变更记录）。
 */
@Slf4j
public final class DataChangeRecorder {

    private DataChangeRecorder() {
    }

    private static final Set<String> EXCLUDED = Set.of(
        "id", "createTime", "updateTime", "deleteTime", "tenantId", "serialVersionUID");

    /**
     * 记录一次更新的字段级变更
     *
     * @param tableName 数据表名（company/candidate/position）
     * @param recordId  记录主键
     * @param before    更新前快照
     * @param after     更新后对象
     * @param operator  操作人（可为 null，null 时跳过记录）
     * @param svc       日志服务（写入方）
     * @param <T>       DO 类型
     */
    public static <T> void record(String tableName, Integer recordId, T before, T after,
                                  UserDO operator, DataChangeLogService svc) {
        if (before == null || after == null || operator == null || svc == null) {
            return;
        }
        try {
            List<DataChangeLogDO> logs = new ArrayList<>();
            for (Field f : after.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || EXCLUDED.contains(f.getName())) {
                    continue;
                }
                f.setAccessible(true);
                Object oldVal = f.get(before);
                Object newVal = f.get(after);
                if (!diff(oldVal, newVal)) {
                    continue;
                }
                DataChangeLogDO entry = new DataChangeLogDO();
                entry.setTableName(tableName);
                entry.setRecordId(recordId);
                entry.setField(f.getName());
                entry.setOldValue(oldVal == null ? null : String.valueOf(oldVal));
                entry.setNewValue(newVal == null ? null : String.valueOf(newVal));
                entry.setOperatorId(operator.getId());
                entry.setOperator(operator.getUsername());
                entry.setCreateTime(LocalDateTime.now());
                logs.add(entry);
            }
            if (!logs.isEmpty()) {
                svc.saveBatch(logs);
            }
        } catch (Exception e) {
            // 变更记录失败不阻断业务主流程
            log.warn("[data-change] 变更记录写入失败：{}", e.getMessage());
        }
    }

    /** null/字符串化后的值对比 */
    private static boolean diff(Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) {
            return false;
        }
        if (oldVal == null || newVal == null) {
            return true;
        }
        return !String.valueOf(oldVal).equals(String.valueOf(newVal));
    }
}
