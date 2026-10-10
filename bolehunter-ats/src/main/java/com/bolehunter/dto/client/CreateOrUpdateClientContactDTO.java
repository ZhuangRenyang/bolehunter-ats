package com.bolehunter.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新客户联系人
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateClientContactDTO {

    @NotBlank(message = "{contact.name.not-blank}")
    @Size(max = 50, message = "{contact.name.not-blank}")
    private String name;

    @Size(max = 20, message = "{contact.name.not-blank}")
    private String phone;

    @Email(message = "{email}")
    @Size(max = 100, message = "{contact.name.not-blank}")
    private String email;

    @Size(max = 50, message = "{contact.name.not-blank}")
    private String title;

    /**
     * 是否主联系人: 1-是 0-否（同公司仅一个）
     */
    private Integer isPrimary;

    @Size(max = 255, message = "{contact.name.not-blank}")
    private String remark;
}
