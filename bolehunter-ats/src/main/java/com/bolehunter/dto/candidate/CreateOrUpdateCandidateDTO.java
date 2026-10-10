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
package com.bolehunter.dto.candidate;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 新增/更新候选人
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateCandidateDTO {

    @NotBlank(message = "{candidate.name.not-blank}")
    @Size(max = 50, message = "{candidate.name.not-blank}")
    private String name;

    /**
     * 手机号（查重键之一）
     */
    @Pattern(regexp = "^$|1[3-9]\\d{9}", message = "{candidate.phone.pattern}")
    @Size(max = 20, message = "{candidate.phone.pattern}")
    private String phone;

    /**
     * 邮箱（查重键之一）
     */
    @Email(message = "{email}")
    @Size(max = 100, message = "{candidate.name.not-blank}")
    private String email;

    /**
     * 当前公司
     */
    @Size(max = 100, message = "{candidate.name.not-blank}")
    private String currentCompany;

    /**
     * 当前职位
     */
    @Size(max = 100, message = "{candidate.name.not-blank}")
    private String currentPosition;

    /**
     * 学历（字典 education 键）
     */
    @Size(max = 32, message = "{candidate.name.not-blank}")
    private String education;

    /**
     * 所在城市
     */
    @Size(max = 50, message = "{candidate.name.not-blank}")
    private String city;

    /**
     * 来源（字典 candidate_source 键）
     */
    @Pattern(regexp = "^$|apply|sourcing|referral|reactive", message = "{candidate.source.illegal}")
    private String source;

    /**
     * 负责顾问用户id（可选，缺省当前登录人）
     */
    private Integer ownerId;

    /**
     * 备注
     */
    @Size(max = 500, message = "{candidate.name.not-blank}")
    private String remark;

    /**
     * 标签id列表（创建/更新时一并设置）
     */
    private List<Integer> tagIds;
}
