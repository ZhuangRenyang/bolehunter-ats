package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 推荐阶段流转历史（append-only，只增不改不删）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommendation_stage_history")
@EqualsAndHashCode(callSuper = true)
public class RecommendationStageHistoryDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐记录id
     */
    private Integer recommendationId;

    /**
     * 原阶段（首次创建为 NULL）
     */
    private String fromStage;

    /**
     * 新阶段
     */
    private String toStage;

    /**
     * 流转时代录的客户反馈
     */
    private String feedback;

    /**
     * 操作人用户id
     */
    private Integer operatorId;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 操作人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String operatorName;
}
