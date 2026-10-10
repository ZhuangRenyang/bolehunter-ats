package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 候选人-标签关联
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("candidate_tag")
@EqualsAndHashCode(callSuper = true)
public class CandidateTagDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 候选人id
     */
    private Integer candidateId;

    /**
     * 标签id
     */
    private Integer tagId;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
