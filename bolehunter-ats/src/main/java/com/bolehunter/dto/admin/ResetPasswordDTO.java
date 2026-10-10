package com.bolehunter.dto.admin;

import com.bolehunter.core.validator.EqualField;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@EqualField(srcField = "newPassword", dstField = "confirmPassword", message = "{password.equal-field}")
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "{password.new.not-blank}")
    @Pattern(regexp = "^[A-Za-z0-9_*&$#@]{6,22}$", message = "{password.new.pattern}")
    private String newPassword;

    @NotBlank(message = "{password.confirm.not-blank}")
    private String confirmPassword;
}
