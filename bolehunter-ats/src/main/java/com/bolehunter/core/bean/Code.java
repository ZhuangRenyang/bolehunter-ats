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
package com.bolehunter.core.bean;

/**
 * 响应码枚举（与原 lin-cms Code 对齐，保证前端错误码兼容）
 */
public enum Code {

    SUCCESS(0, "成功"),
    CREATED(1, "创建成功"),
    UPDATED(2, "更新成功"),
    DELETED(3, "删除成功"),
    PARAMETER_ERROR(10030, "参数错误"),
    NOT_FOUND(10020, "资源不存在"),
    FAIL(9999, "服务器未知错误"),
    FILE_TOO_LARGE(10110, "文件体积过大"),
    INTERNAL_SERVER_ERROR(9999, "服务器未知错误");

    private final int code;

    private final String description;

    Code(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getZhDescription() {
        return description;
    }
}
