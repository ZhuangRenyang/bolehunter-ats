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
package ${packageName}.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.model.${className}DO;

import java.util.List;

/**
 * ${businessName} 服务
 */
public interface ${className}Service {

    /**
     * 分页查询（默认按 id 倒序；业务筛选条件请在实现中按需补充 QueryWrapper）
     */
    IPage<${className}DO> getPage(Integer page, Integer count);

    /**
     * 详情
     */
    ${className}DO getById(${pkJavaType} id);

    /**
     * 新增
     */
    ${className}DO create(${className}DO ${classNameCamel});

    /**
     * 更新
     */
    ${className}DO update(${className}DO ${classNameCamel});

    /**
     * 删除
     */
    void delete(${pkJavaType} id);
}
