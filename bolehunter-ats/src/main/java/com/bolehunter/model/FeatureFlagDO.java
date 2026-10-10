package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 功能开关
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("feature_flag")
@EqualsAndHashCode(callSuper = true)
public class FeatureFlagDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开关键（唯一）
     */
    private String flagKey;

    /**
     * 开关名称
     */
    private String flagName;

    /**
     * 说明
     */
    private String description;

    /**
     * 是否开启: 1-开 0-关
     */
    private Integer enabled;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
