package com.bolehunter.dto.admin;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
public class UpdateUserInfoDTO {

    @NotEmpty(message = "{group.ids.not-empty}")
    private List<@Min(1) Integer> groupIds;

    /**
     * 用户类型（可选）: ADMIN-管理员 CONSULTANT-顾问 CLIENT_HR-客户HR READONLY-只读
     */
    @Pattern(regexp = "ADMIN|CONSULTANT|CLIENT_HR|READONLY", message = "{user.type.illegal}")
    private String userType;

    /**
     * 部门id（可选）
     */
    @Min(value = 1, message = "{id.positive}")
    private Integer deptId;

}
