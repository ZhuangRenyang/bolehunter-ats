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
 * 登录日志（迭代 32 安全审计）
 */
@Data
@TableName("sys_login_log")
public class LoginLogDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 登录用户名 */
    private String username;

    /** 用户id（用户不存在时为空） */
    private Integer userId;

    /** 登录IP */
    private String ip;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 结果：true 成功 / false 失败 */
    private Boolean result;

    /** 失败原因 */
    private String failReason;

    /** 耗时毫秒 */
    private Integer elapsedMs;

    /** 时间 */
    private LocalDateTime createTime;
}
