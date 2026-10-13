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
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.mapper.ClientContactMapper;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.mapper.PositionMapper;
import com.bolehunter.mapper.RecommendationMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.ClientContactDO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.PositionDO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.ClientContactService;
import com.bolehunter.service.ClientService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户 CRM 服务实现
 */
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClientContactService clientContactService;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private ClientContactMapper clientContactMapper;

    @Override
    public IPage<CompanyDO> getClientPage(String keyword, String industry, String level,
                                          Integer ownerId, String clientStatus, Integer page, Integer count) {
        Page<CompanyDO> pager = new Page<>(page, count);
        QueryWrapper<CompanyDO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(keyword != null && !keyword.isEmpty(), CompanyDO::getName, keyword)
                .eq(industry != null && !industry.isEmpty(), CompanyDO::getIndustry, industry)
                .eq(level != null && !level.isEmpty(), CompanyDO::getLevel, level)
                .eq(ownerId != null, CompanyDO::getOwnerId, ownerId)
                .eq(clientStatus != null && !clientStatus.isEmpty(), CompanyDO::getClientStatus, clientStatus)
                .orderByDesc(CompanyDO::getId);
        return companyMapper.selectPage(pager, wrapper);
    }

    @Override
    public Map<String, Object> getClientDetail(Integer id) {
        CompanyDO company = companyMapper.selectById(id);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("company", company);
        detail.put("contacts", clientContactService.listByCompany(id));
        QueryWrapper<PositionDO> positionWrapper = new QueryWrapper<>();
        positionWrapper.lambda().eq(PositionDO::getCompanyId, id)
                .orderByDesc(PositionDO::getReleaseDate);
        detail.put("positions", positionMapper.selectList(positionWrapper));
        return detail;
    }

    @Override
    public boolean createClient(CreateOrUpdateCompanyDTO validator, Integer currentUserId) {
        CompanyDO company = new CompanyDO()
                .setName(validator.getName())
                .setForeignName(validator.getForeignName())
                .setCity(validator.getCity())
                .setLogo(validator.getLogo())
                .setPoster(validator.getPoster())
                .setMission(validator.getMission())
                .setDescription(validator.getDescription())
                .setIndustry(validator.getIndustry())
                .setLevel(validator.getLevel())
                // 客户建档直接生效，不走老的认证审核流
                .setState(1)
                .setClientStatus(validator.getClientStatus() == null ? "potential" : validator.getClientStatus())
                // 负责人缺省为当前登录顾问
                .setOwnerId(validator.getOwnerId() == null ? currentUserId : validator.getOwnerId());
        return companyMapper.insert(company) > 0;
    }

    @Override
    public boolean updateClient(CompanyDO company, CreateOrUpdateCompanyDTO validator) {
        company.setName(validator.getName());
        company.setForeignName(validator.getForeignName());
        company.setCity(validator.getCity());
        company.setLogo(validator.getLogo());
        company.setPoster(validator.getPoster());
        company.setMission(validator.getMission());
        company.setDescription(validator.getDescription());
        if (validator.getIndustry() != null) {
            company.setIndustry(validator.getIndustry());
        }
        if (validator.getLevel() != null) {
            company.setLevel(validator.getLevel());
        }
        if (validator.getClientStatus() != null) {
            company.setClientStatus(validator.getClientStatus());
        }
        if (validator.getOwnerId() != null) {
            company.setOwnerId(validator.getOwnerId());
        }
        return companyMapper.updateById(company) > 0;
    }

    @Override
    public boolean assignOwner(Integer id, Integer ownerId) {
        UserDO owner = userMapper.selectById(ownerId);
        if (owner == null) {
            throw new ForbiddenException(80160);
        }
        CompanyDO patch = new CompanyDO();
        patch.setId(id);
        patch.setOwnerId(ownerId);
        return companyMapper.updateById(patch) > 0;
    }

    @Override
    public boolean changeClientStatus(Integer id, String clientStatus) {
        CompanyDO patch = new CompanyDO();
        patch.setId(id);
        patch.setClientStatus(clientStatus);
        return companyMapper.updateById(patch) > 0;
    }

    @Override
    public CompanyDO getById(Integer id) {
        return companyMapper.selectById(id);
    }

    @Override
    public List<CompanyDO> getClientOptions() {
        QueryWrapper<CompanyDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().select(CompanyDO::getId, CompanyDO::getName, CompanyDO::getLevel,
                        CompanyDO::getClientStatus)
                .orderByDesc(CompanyDO::getId);
        return companyMapper.selectList(wrapper);
    }

    @Override
    public List<UserDO> getOwnerOptions() {
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(UserDO::getUserType, Arrays.asList("ADMIN", "CONSULTANT"))
                .orderByAsc(UserDO::getId);
        return userMapper.selectList(wrapper);
    }

    @Transactional
    @Override
    public boolean deleteClient(Integer id) {
        CompanyDO company = companyMapper.selectById(id);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        // 关联防护：名下任一职位存在推荐记录（含已归档）时禁止删除，
        // 避免推荐看板/数据报表出现"幽灵客户"导致统计口径失真
        Long refCount = recommendationMapper.selectCount(
                Wrappers.<RecommendationDO>lambdaQuery().eq(RecommendationDO::getClientId, id));
        if (refCount != null && refCount > 0) {
            throw new ForbiddenException(80290);
        }
        // 通过校验后级联软删：职位 → 联系人 → 客户本体（三者均为 MP 逻辑删除）
        positionMapper.delete(Wrappers.<PositionDO>lambdaQuery().eq(PositionDO::getCompanyId, id));
        clientContactMapper.delete(
                Wrappers.<ClientContactDO>lambdaQuery().eq(ClientContactDO::getCompanyId, id));
        return companyMapper.deleteById(id) > 0;
    }
}
