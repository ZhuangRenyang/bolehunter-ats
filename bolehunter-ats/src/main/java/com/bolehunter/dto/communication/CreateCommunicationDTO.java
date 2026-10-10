package com.bolehunter.dto.communication;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新增沟通记录
 */
@Data
@NoArgsConstructor
public class CreateCommunicationDTO {

    @NotNull(message = "{id.positive}")
    private Integer recommendationId;

    @Pattern(regexp = "phone|wechat|email|meeting|note|other", message = "{candidate.source.illegal}")
    private String type;

    @NotBlank(message = "{contact.name.not-blank}")
    @Size(max = 1000, message = "{contact.name.not-blank}")
    private String content;
}
