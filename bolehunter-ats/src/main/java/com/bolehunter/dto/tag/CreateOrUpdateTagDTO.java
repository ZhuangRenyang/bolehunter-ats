package com.bolehunter.dto.tag;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新标签
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateTagDTO {

    @NotBlank(message = "{tag.name.not-blank}")
    @Size(max = 50, message = "{tag.name.not-blank}")
    private String name;
}
