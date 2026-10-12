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
package ${packageName}.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.${moduleName}.${className}CreateDTO;
import com.bolehunter.dto.${moduleName}.${className}UpdateDTO;
import com.bolehunter.model.${className}DO;
import com.bolehunter.service.${className}Service;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${businessName} 管理
 */
@RestController
@RequestMapping("/recruit/${moduleName}")
public class ${className}Controller {

    private final ${className}Service ${classNameCamel}Service;

    public ${className}Controller(${className}Service ${classNameCamel}Service) {
        this.${classNameCamel}Service = ${classNameCamel}Service;
    }

    /**
     * ${businessName} 分页列表（业务筛选条件请按需补充 @RequestParam 与 QueryWrapper）
     */
    @Logger(template = "查询${businessName}列表")
    @GetMapping("/page")
    @PermissionMeta(value = "查询${businessName}列表")
    public PageResponseVO<${className}DO> page(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<${className}DO> iPage = ${classNameCamel}Service.getPage(page, count);
        return PageUtil.build(iPage);
    }

    /**
     * ${businessName} 详情
     */
    @Logger(template = "查询${businessName}详情")
    @GetMapping("/{id}")
    @PermissionMeta(value = "查询${businessName}详情")
    public ${className}DO get(@PathVariable("id") @Positive(message = "{id.positive}") ${pkJavaType} id) {
        return ${classNameCamel}Service.getById(id);
    }

    /**
     * 创建${businessName}
     */
    @Logger(template = "新增${businessName}")
    @PostMapping
    @PermissionMeta(value = "新增${businessName}")
    public CreatedVO create(@RequestBody @Valid ${className}CreateDTO dto) {
        ${className}DO ${classNameCamel} = new ${className}DO();
        BeanUtils.copyProperties(dto, ${classNameCamel});
        ${classNameCamel}Service.create(${classNameCamel});
        return new CreatedVO();
    }

    /**
     * 更新${businessName}
     */
    @Logger(template = "更新${businessName}")
    @PutMapping("/{id}")
    @PermissionMeta(value = "更新${businessName}")
    public UpdatedVO update(@PathVariable("id") @Positive(message = "{id.positive}") ${pkJavaType} id,
                            @RequestBody @Valid ${className}UpdateDTO dto) {
        ${className}DO ${classNameCamel} = new ${className}DO();
        BeanUtils.copyProperties(dto, ${classNameCamel});
        ${classNameCamel}Service.update(${classNameCamel});
        return new UpdatedVO();
    }

    /**
     * 删除${businessName}
     */
    @Logger(template = "删除${businessName}")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除${businessName}")
    public DeletedVO delete(@PathVariable("id") @Positive(message = "{id.positive}") ${pkJavaType} id) {
        ${classNameCamel}Service.delete(id);
        return new DeletedVO();
    }
}
