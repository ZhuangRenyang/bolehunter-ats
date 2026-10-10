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
import com.bolehunter.dto.client.CreateOrUpdateClientContactDTO;
import com.bolehunter.mapper.ClientContactMapper;
import com.bolehunter.model.ClientContactDO;
import com.bolehunter.service.ClientContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系人服务实现
 */
@Service
public class ClientContactServiceImpl implements ClientContactService {

    @Autowired
    private ClientContactMapper clientContactMapper;

    @Override
    public List<ClientContactDO> listByCompany(Integer companyId) {
        QueryWrapper<ClientContactDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ClientContactDO::getCompanyId, companyId)
                .orderByDesc(ClientContactDO::getIsPrimary)
                .orderByAsc(ClientContactDO::getId);
        return clientContactMapper.selectList(wrapper);
    }

    @Transactional
    @Override
    public boolean createContact(Integer companyId, CreateOrUpdateClientContactDTO validator) {
        ClientContactDO contact = ClientContactDO.builder()
                .companyId(companyId)
                .name(validator.getName())
                .phone(validator.getPhone())
                .email(validator.getEmail())
                .title(validator.getTitle())
                .isPrimary(validator.getIsPrimary() != null && validator.getIsPrimary() == 1 ? 1 : 0)
                .remark(validator.getRemark())
                .build();
        int rows = clientContactMapper.insert(contact);
        if (rows > 0 && contact.getIsPrimary() == 1) {
            clearOtherPrimary(companyId, contact.getId());
        }
        return rows > 0;
    }

    @Transactional
    @Override
    public boolean updateContact(ClientContactDO contact, CreateOrUpdateClientContactDTO validator) {
        contact.setName(validator.getName());
        contact.setPhone(validator.getPhone());
        contact.setEmail(validator.getEmail());
        contact.setTitle(validator.getTitle());
        if (validator.getIsPrimary() != null) {
            contact.setIsPrimary(validator.getIsPrimary() == 1 ? 1 : 0);
        }
        contact.setRemark(validator.getRemark());
        boolean ok = clientContactMapper.updateById(contact) > 0;
        if (ok && contact.getIsPrimary() != null && contact.getIsPrimary() == 1) {
            clearOtherPrimary(contact.getCompanyId(), contact.getId());
        }
        return ok;
    }

    @Override
    public boolean deleteContact(Integer id) {
        return clientContactMapper.deleteById(id) > 0;
    }

    @Override
    public ClientContactDO getById(Integer id) {
        return clientContactMapper.selectById(id);
    }

    /**
     * 将同公司其他联系人的主联系人标记清零（保证主联系人唯一）
     */
    private void clearOtherPrimary(Integer companyId, Integer excludeId) {
        ClientContactDO patch = new ClientContactDO();
        patch.setIsPrimary(0);
        QueryWrapper<ClientContactDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ClientContactDO::getCompanyId, companyId)
                .ne(ClientContactDO::getId, excludeId);
        clientContactMapper.update(patch, wrapper);
    }
}
