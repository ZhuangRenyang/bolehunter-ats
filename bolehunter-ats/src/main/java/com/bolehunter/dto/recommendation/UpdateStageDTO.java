package com.bolehunter.dto.recommendation;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 推荐阶段流转
 */
@Data
@NoArgsConstructor
public class UpdateStageDTO {

    @NotBlank(message = "{recommendation.stage.illegal}")
    @Pattern(regexp = "PENDING|RECOMMENDED|CLIENT_READ|INTERVIEW_PENDING|INTERVIEWING|OFFER_PENDING|OFFERED|ONBOARDED|PAID|ARCHIVED",
            message = "{recommendation.stage.illegal}")
    private String stage;

    /**
     * 本次流转同时代录的客户反馈（可选）
     */
    private String feedback;
}
