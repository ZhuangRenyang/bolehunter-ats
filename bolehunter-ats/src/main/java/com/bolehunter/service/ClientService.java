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
