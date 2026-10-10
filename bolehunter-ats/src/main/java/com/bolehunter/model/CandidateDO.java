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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 候选人
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("candidate")
@EqualsAndHashCode(callSuper = true)
public class CandidateDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号（查重键之一）
     */
    private String phone;

    /**
     * 邮箱（查重键之一）
     */
    private String email;

    /**
     * 当前公司
     */
    private String currentCompany;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 学历（字典 education）
     */
    private String education;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 来源（字典 candidate_source）：apply/sourcing/referral/reactive
     */
    private String source;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;

    /**
     * 原始简历文件相对路径（assets 下）
     */
    private String resumeFile;

    /**
     * 自定义字段扩展数据（JSON 字符串，迭代4接字段定义）
     */
    private String extJson;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 标签名列表（非数据库字段，查询时填充）
     */
    @TableField(exist = false)
    private List<String> tagNames;

    /**
     * 标签id列表（非数据库字段，编辑时回显）
     */
    @TableField(exist = false)
    private List<Integer> tagIds;
}
