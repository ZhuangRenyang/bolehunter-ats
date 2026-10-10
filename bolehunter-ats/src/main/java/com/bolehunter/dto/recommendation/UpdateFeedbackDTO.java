package com.bolehunter.dto.recommendation;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * 客户反馈代录
 */
@Data
@NoArgsConstructor
public class UpdateFeedbackDTO {

    @Size(max = 5000, message = "{candidate.name.not-blank}")
    private String feedback;
}
