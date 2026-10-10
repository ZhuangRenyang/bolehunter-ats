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
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Eval
 * @since 2022-04-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("application")
public class ApplicationDO extends BaseModel {

    /**
     * 状态
     */
    private Integer state;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 简历id
     */
    private Integer resumeId;

    /**
     * 职位id
     */
    private Integer positionId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * HR id
     */
    private Integer hrId;

    /**
     * 公司 id
     */
    private Integer companyId;
}
