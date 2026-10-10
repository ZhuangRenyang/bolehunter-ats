package com.bolehunter.dto.dict;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新字典类型
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateDictTypeDTO {

    @NotBlank(message = "{dict.type.name.not-blank}")
    @Size(max = 64, message = "{dict.type.name.not-blank}")
    private String dictName;

    /**
     * 字典类型键，如 client_level
     */
    @NotBlank(message = "{dict.type.key.not-blank}")
    @Size(max = 64, message = "{dict.type.key.not-blank}")
    private String dictType;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    @Size(max = 255, message = "{dict.type.name.not-blank}")
    private String remark;
}
