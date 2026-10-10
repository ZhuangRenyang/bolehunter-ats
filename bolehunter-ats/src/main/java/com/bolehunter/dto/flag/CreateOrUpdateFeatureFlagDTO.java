package com.bolehunter.dto.flag;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新功能开关
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateFeatureFlagDTO {

    /**
     * 开关键，如 resume_parse
     */
    @NotBlank(message = "{flag.key.not-blank}")
    @Size(max = 64, message = "{flag.key.not-blank}")
    private String flagKey;

    @NotBlank(message = "{flag.name.not-blank}")
    @Size(max = 64, message = "{flag.name.not-blank}")
    private String flagName;

    @Size(max = 255, message = "{flag.name.not-blank}")
    private String description;

    /**
     * 是否开启: 1-开 0-关
     */
    private Integer enabled;
}
