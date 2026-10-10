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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.UserDO;

import java.util.List;
import java.util.Map;

/**
 * 客户 CRM 服务（猎头视角的客户公司）
 */
public interface ClientService {

    /**
     * 客户分页：支持关键词/行业/等级/负责人/合作状态筛选
     */
    IPage<CompanyDO> getClientPage(String keyword, String industry, String level,
                                   Integer ownerId, String clientStatus, Integer page, Integer count);

    /**
     * 客户详情聚合：company + contacts + positions
     */
    Map<String, Object> getClientDetail(Integer id);

    /**
     * 顾问直接建客户档案（不走老的认证审核流，state 固定为 1）
     */
    boolean createClient(CreateOrUpdateCompanyDTO validator, Integer currentUserId);

    boolean updateClient(CompanyDO company, CreateOrUpdateCompanyDTO validator);

    boolean assignOwner(Integer id, Integer ownerId);

    boolean changeClientStatus(Integer id, String clientStatus);

    CompanyDO getById(Integer id);

    /**
     * 客户下拉选项（id/name/level，供职位表单等使用）
     */
    List<CompanyDO> getClientOptions();

    /**
     * 可分配负责人选项（ADMIN/CONSULTANT 用户）
     */
    List<UserDO> getOwnerOptions();
}
