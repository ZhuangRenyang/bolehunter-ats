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

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * @author ZhuangRenyang
 * @date 2022-03-24
 */
@Data
@Accessors(chain = true)
@TableName("experience")
public class ExperienceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 简历id
     */
    private Integer resumeId;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 职位
     */
    private String position;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 工作描述
     */
    private String performance;

}
