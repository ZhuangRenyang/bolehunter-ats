/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ${_meta.author}
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 *
 * 由 BoleHunter 代码生成器生成于 ${date}，模块：${moduleName}（${businessName}）
 */
package ${packageName}.dto.${moduleName};

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
<#if hasDateField>import com.fasterxml.jackson.annotation.JsonFormat;
</#if>import lombok.Data;

import java.io.Serializable;
<#if hasBigDecimalField>import java.math.BigDecimal;
</#if><#if hasDateField>import java.util.Date;
</#if>
/**
 * ${businessName} 新增 DTO（字段校验按表结构自动推断）
 */
@Data
public class ${className}CreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

<#list bizColumns as c>
    /** ${c.comment} */
<#if c.isText>
<#if c.isRequired>    @NotBlank(message = "${c.comment}不能为空")
</#if>    private String ${c.fieldName};
<#elseif c.isVarchar>
<#if c.isRequired>    @NotBlank(message = "${c.comment}不能为空")
</#if><#if c.length gt 0 && c.length lte 5000>    @Size(max = ${c.length}, message = "${c.comment}不能超过 ${c.length} 个字符")
</#if>    private String ${c.fieldName};
<#elseif c.isDateOnly>
<#if c.isRequired>    @NotNull(message = "${c.comment}不能为空")
</#if>    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private ${c.javaType} ${c.fieldName};
<#elseif c.javaType == "Date">
<#if c.isRequired>    @NotNull(message = "${c.comment}不能为空")
</#if>    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ${c.fieldName};
<#elseif c.javaType == "java.math.BigDecimal">
<#if c.isRequired>    @NotNull(message = "${c.comment}不能为空")
</#if>    private java.math.BigDecimal ${c.fieldName};
<#else>
<#if c.isRequired>    @NotNull(message = "${c.comment}不能为空")
</#if>    private ${c.javaType} ${c.fieldName};
</#if>
</#list>
}
