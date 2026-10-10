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
package com.bolehunter.vo;

import com.bolehunter.model.EducationDO;
import com.bolehunter.model.ExperienceDO;
import com.bolehunter.model.ProjectDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 统一API响应结果封装
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeVO {
    private Integer resumeId;
    // 用户昵称
    private String name;
    //头像
    private String avatar;
    // 求职意向
    private String jobIntention;
    // 性别
    private int sex;

    // 居住地
    private String home;
    // 年龄
    private int age;
    // 英语水平
    private String english;
    // 电话
    private String tel;
    // 邮箱
    private String email;
    // 专业技能
    private String ability;
    // 个人总结
    private String personalSummary;

    //教育经历
    private List<EducationDO> educations;

    //项目经历
    private List<ProjectDO> projects;

    //工作经历
    private List<ExperienceDO> experiences;

}
