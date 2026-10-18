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
package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据变更日志（迭代 32 安全审计）：字段级变更记录（company/candidate/position）
 */
@Data
@TableName("data_change_log")
public class DataChangeLogDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 数据表名（company/candidate/position） */
    private String tableName;

    /** 记录主键 */
    private Integer recordId;

    /** 变更字段（Java 属性名） */
    private String field;

    /** 旧值 */
    private String oldValue;

    /** 新值 */
    private String newValue;

    /** 操作人id */
    private Integer operatorId;

    /** 操作人用户名 */
    private String operator;

    /** 时间 */
    private LocalDateTime createTime;
}
