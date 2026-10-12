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
package ${packageName}.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
<#if hasBaseColumns>import com.bolehunter.model.BaseModel;
</#if>import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
<#list columns as c><#if c.javaType == "Date" || c.javaType == "java.time.LocalDate" || c.javaType == "java.math.BigDecimal">import ${c.javaType};
</#if></#list>
/**
 * ${businessName} 数据对象（表 ${tableName}）
<#if hasBaseColumns> * 继承 BaseModel：含 id / create_time / update_time / delete_time（逻辑删除）
</#if> */
@Data
@NoArgsConstructor
@TableName("${tableName}")
<#if hasBaseColumns>@EqualsAndHashCode(callSuper = true)
public class ${className}DO extends BaseModel {
<#else>public class ${className}DO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
</#if>
<#list columns as c><#if !hasBaseColumns || !(c.columnName == "id" || c.columnName == "create_time" || c.columnName == "update_time" || c.columnName == "delete_time")>
    /** ${c.comment} */
<#if c.isPk>    @TableId(value = "${c.columnName}", type = IdType.AUTO)
<#else>    @TableField("${c.columnName}")
</#if>    private ${c.javaType} ${c.fieldName};
</#if></#list>
}
