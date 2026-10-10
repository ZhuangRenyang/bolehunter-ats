package com.bolehunter.dto.category;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author Eval
 * @create 2021/4/4 13:03
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateCategoryDTO {
    @NotEmpty(message = "{category.name.not-empty}")
    private String name;

    @NotEmpty(message = "{category.description.not-empty}")
    private String description;
}
