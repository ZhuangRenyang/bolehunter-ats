/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.service.CompanyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-01
 */
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, CompanyDO> implements CompanyService {

    @Resource
    private CompanyMapper companyMapper;

    @Override
    public boolean updateCompany(CompanyDO company, CreateOrUpdateCompanyDTO validator) {
        company.setName(validator.getName());
        company.setForeignName(validator.getForeignName());
        company.setCity(validator.getCity());
        company.setLogo(validator.getLogo());
        company.setPoster(validator.getPoster());
        company.setMission(validator.getMission());
        company.setDescription(validator.getDescription());
        company.setState(validator.getState());
        return companyMapper.updateById(company) > 0;
    }

    @Override
    public boolean updateState(Integer id, Integer state) {
        return companyMapper.updateState(id, state);
    }

    @Override
    public boolean createCompany(CreateOrUpdateCompanyDTO validator) {
        CompanyDO companyDO = new CompanyDO();
        companyDO.setName(validator.getName());
        companyDO.setForeignName(validator.getForeignName());
        companyDO.setCity(validator.getCity());
        companyDO.setLogo(validator.getLogo());
        companyDO.setPoster(validator.getPoster());
        companyDO.setMission(validator.getMission());
        companyDO.setDescription(validator.getDescription());
        // 新增企业，状态默认都为0，表示未审核
        companyDO.setState(0);
        return companyMapper.insert(companyDO) > 0;
    }

    @Override
    public CompanyDO getByName(String name) {
        QueryWrapper<CompanyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        CompanyDO companyDO = companyMapper.selectOne(wrapper);
        return companyDO;
    }

    @Override
    public List<CompanyDO> findHotCompany() {
        return companyMapper.findHotCompany();
    }
}
