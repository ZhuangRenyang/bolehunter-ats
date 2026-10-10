package com.bolehunter.dto.dict;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新字典数据
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateDictDataDTO {

    /**
     * 所属字典类型键
     */
    @NotBlank(message = "{dict.type.key.not-blank}")
    @Size(max = 64, message = "{dict.type.key.not-blank}")
    private String dictType;

    @NotBlank(message = "{dict.data.label.not-blank}")
    @Size(max = 64, message = "{dict.data.label.not-blank}")
    private String dictLabel;

    @NotBlank(message = "{dict.data.value.not-blank}")
    @Size(max = 64, message = "{dict.data.value.not-blank}")
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer dictSort;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    @Size(max = 255, message = "{dict.data.label.not-blank}")
    private String remark;
}
