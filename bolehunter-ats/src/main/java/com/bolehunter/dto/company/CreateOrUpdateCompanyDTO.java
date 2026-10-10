package com.bolehunter.dto.company;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author Eval
 * @create 2021/4/4 13:14
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateCompanyDTO {

    @NotEmpty(message = "{company.name.not-empty}")
    private String name;

    private String foreignName;

    @NotEmpty(message = "{company.city.not-empty}")
    private String city;

    private String logo;

    private String poster;

    private String mission;

    private String description;

    private Integer state;

    /**
     * 所属行业（字典 industry 键，可选）
     */
    @Size(max = 32, message = "{company.industry.length}")
    private String industry;

    /**
     * 客户等级（字典 client_level 键，可选）
     */
    @Size(max = 32, message = "{company.level.length}")
    private String level;

    /**
     * 客户合作状态（字典 client_status 键，可选，默认 potential）
     */
    @Pattern(regexp = "potential|signing|suspended|lost", message = "{client.status.illegal}")
    private String clientStatus;

    /**
     * 负责顾问用户id（可选）
     */
    @Min(value = 1, message = "{id.positive}")
    private Integer ownerId;
}
