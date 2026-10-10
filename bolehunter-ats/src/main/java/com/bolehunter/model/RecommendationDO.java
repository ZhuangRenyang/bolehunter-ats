package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 推荐记录（核心表：职位 × 候选人 × 阶段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommendation")
@EqualsAndHashCode(callSuper = true)
public class RecommendationDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职位id
     */
    private Integer jobId;

    /**
     * 候选人id
     */
    private Integer candidateId;

    /**
     * 客户公司id（自职位冗余）
     */
    private Integer clientId;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;

    /**
     * 当前阶段（字典 recommendation_stage）
     */
    private String currentStage;

    /**
     * 进入当前阶段时间
     */
    private Date stageTime;

    /**
     * 客户反馈记录（顾问代录）
     */
    private String feedback;

    /**
     * 最近反馈时间
     */
    private Date feedbackTime;

    /**
     * 免登录确认链接 token（v1.1 预留，本期不使用）
     */
    private String confirmToken;

    /**
     * 确认链接有效期（v1.1 预留，本期不使用）
     */
    private Date confirmExpire;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    // ---------------- 展示字段（非数据库列，批量填充避免 N+1） ----------------

    @TableField(exist = false)
    private String candidateName;

    @TableField(exist = false)
    private String candidateCompany;

    @TableField(exist = false)
    private String candidatePosition;

    @TableField(exist = false)
    private String jobTitle;

    @TableField(exist = false)
    private String clientName;

    @TableField(exist = false)
    private String ownerName;
}
