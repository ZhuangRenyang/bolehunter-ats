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
package ${packageName}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bolehunter.mapper.${className}Mapper;
import com.bolehunter.model.${className}DO;
import com.bolehunter.service.${className}Service;
import org.springframework.stereotype.Service;

/**
 * ${businessName} 服务实现
 */
@Service
public class ${className}ServiceImpl implements ${className}Service {

    private final ${className}Mapper ${classNameCamel}Mapper;

    public ${className}ServiceImpl(${className}Mapper ${classNameCamel}Mapper) {
        this.${classNameCamel}Mapper = ${classNameCamel}Mapper;
    }

    @Override
    public IPage<${className}DO> getPage(Integer page, Integer count) {
        // TODO 按业务补充筛选条件，例如：
        // wrapper.lambda().like(StringUtils.hasText(keyword), ${className}DO::getName, keyword);
        QueryWrapper<${className}DO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByDesc(${className}DO::get${pkFieldName?cap_first});
        return ${classNameCamel}Mapper.selectPage(new Page<>(page, count), wrapper);
    }

    @Override
    public ${className}DO getById(${pkJavaType} id) {
        return ${classNameCamel}Mapper.selectById(id);
    }

    @Override
    public ${className}DO create(${className}DO ${classNameCamel}) {
        ${classNameCamel}Mapper.insert(${classNameCamel});
        return ${classNameCamel};
    }

    @Override
    public ${className}DO update(${className}DO ${classNameCamel}) {
        ${classNameCamel}Mapper.updateById(${classNameCamel});
        return getById(${classNameCamel}.get${pkFieldName?cap_first}());
    }

    @Override
    public void delete(${pkJavaType} id) {
        ${classNameCamel}Mapper.deleteById(id);
    }
}
