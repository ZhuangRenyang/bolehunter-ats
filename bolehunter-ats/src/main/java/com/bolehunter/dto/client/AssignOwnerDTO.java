package com.bolehunter.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 分配客户负责人
 */
@Data
@NoArgsConstructor
public class AssignOwnerDTO {

    @NotNull(message = "{id.positive}")
    @Min(value = 1, message = "{id.positive}")
    private Integer ownerId;
}
