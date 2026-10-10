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
package com.bolehunter.model;

import com.bolehunter.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Eval
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("company")
public class CompanyDO extends BaseModel {

    /**
     * 公司名称
     */
    private String name;

    /**
     * 外文名
     */
    private String foreignName;

    /**
     * 总部地点
     */
    private String city;

    /**
     * 公司logo
     */
    private String logo;

    /**
     * 公司海报
     */
    private String poster;

    /**
     * 使命
     */
    private String mission;

    /**
     * 公司简介
     */
    private String description;

    /**
     * 是否审核，0未审核，1审核通过，2审核不通过
     */
    private Integer state;

    /**
     * 所属行业（字典 industry）
     */
    private String industry;

    /**
     * 客户等级（字典 client_level）
     */
    private String level;

    /**
     * 客户合作状态（字典 client_status）：potential/signing/suspended/lost
     */
    private String clientStatus;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;
}
