/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.dept.CreateOrUpdateDeptDTO;
import com.bolehunter.model.DeptDO;

import java.util.List;

/**
 * 部门服务
 */
public interface DeptService extends IService<DeptDO> {

    boolean createDept(CreateOrUpdateDeptDTO validator);

    boolean updateDept(DeptDO dept, CreateOrUpdateDeptDTO validator);

    boolean deleteDept(Integer id);

    /**
     * 查询全部部门（平铺，按 sort 升序），供下拉选择
     */
    List<DeptDO> getAllDepts();

    /**
     * 查询部门树（children 嵌套）
     */
    List<DeptDO> getDeptTree();
}
