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

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 招聘职位实体
 * @author Eval
 * @since 2022-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("position")
public class PositionDO extends BaseModel {

    /**
     * 职位名称
     */
    private String title;

    /**
     * 学历要求
     */
    private String eduBack;

    /**
     * 职位要求
     */
    private String requirement;

    /**
     * 招聘人数
     */
    private Integer quantity;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 最高薪资
     */
    private Integer salaryUp;

    /**
     * 最低薪资
     */
    private Integer salaryDown;

    /**
     * 职位发布时间
     */
    private LocalDateTime releaseDate;

    /**
     * 是否审核，0未审核，1审核通过，2审核不通过
     */
    private Integer state;

    /**
     * 职位浏览量
     */
    private Integer hits;

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * HR id
     */
    private Integer hrId;

    /**
     * 公司id
     */
    private Integer companyId;
}
