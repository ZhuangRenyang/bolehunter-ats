package com.bolehunter.dto.recommendation;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增推荐
 */
@Data
@NoArgsConstructor
public class CreateRecommendationDTO {

    @NotNull(message = "{id.positive}")
    @Min(value = 1, message = "{id.positive}")
    private Integer jobId;

    @NotNull(message = "{id.positive}")
    @Min(value = 1, message = "{id.positive}")
    private Integer candidateId;

    /**
     * 负责顾问用户id（可选，缺省当前登录人）
     */
    private Integer ownerId;

    @Size(max = 500, message = "{candidate.name.not-blank}")
    private String remark;
}
