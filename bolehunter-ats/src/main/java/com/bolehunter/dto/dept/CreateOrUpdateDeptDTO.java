package com.bolehunter.dto.dept;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新部门
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateDeptDTO {

    /**
     * 父部门id，0 为顶级
     */
    @Min(value = 0, message = "{id.positive}")
    private Integer parentId;

    @NotBlank(message = "{dept.name.not-blank}")
    @Size(max = 50, message = "{dept.name.not-blank}")
    private String deptName;

    private Integer sort;

    @Size(max = 50, message = "{dept.name.not-blank}")
    private String leader;

    @Size(max = 20, message = "{dept.name.not-blank}")
    private String phone;
}
