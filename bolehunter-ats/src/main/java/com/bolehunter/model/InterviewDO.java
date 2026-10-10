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
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Eval
 * @since 2022-04-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("interview")
@EqualsAndHashCode(callSuper = true)
public class InterviewDO extends BaseModel implements Serializable {

    /**
     *  简历编号
     */
    private Integer resumeId;

    /**
     *  求职者ID
     */
    private Integer userId;

    /**
     * 公司ID
     */
    private Integer companyId;

    /**
     * hrID
     */
    private Integer hrId;

    /**
     *  职位ID
     */
    private Integer positionId;

    /**
     * 面试时间
     */
    private Date time;

    /**
     * 面试地点
     */
    private String address;

    /**
     *  面试备注
     */
    private String memo;

    /**
     *  面试评价
     */
    private String comments;

    /**
     *  面试状态
     */
    private Integer status;
}