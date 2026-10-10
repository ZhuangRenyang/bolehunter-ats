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
package com.bolehunter.model.result;

import com.bolehunter.model.ApplicationDO;
import lombok.Data;

/**
 * @author Eval
 * @since 2022-04-02
 */
@Data
public class ApplicationResultDO extends ApplicationDO {

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 电话
     */
    private String tel;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 简历分数
     */
    private Integer grade;

    /**
     * 工作地点
     */
    private String positionCity;

    /**
     * 最低薪资
     */
    private Integer salaryDown;

    /**
     * 最高薪资
     */
    private Integer salaryUp;

    /**
     * 公司名称
     */
    private String name;
}
