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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.communication.CreateCommunicationDTO;
import com.bolehunter.mapper.CommunicationMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.CommunicationDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.CommunicationService;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 沟通记录服务实现
 */
@Service
public class CommunicationServiceImpl extends ServiceImpl<CommunicationMapper, CommunicationDO>
        implements CommunicationService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<CommunicationDO> getByRecommendation(Integer recommendationId, Integer page, Integer count) {
        Page<CommunicationDO> pager = new Page<>(page, count);
        QueryWrapper<CommunicationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(CommunicationDO::getRecommendationId, recommendationId)
                .orderByDesc(CommunicationDO::getCommunicateTime);
        IPage<CommunicationDO> result = this.page(pager, wrapper);
        List<CommunicationDO> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Integer> userIds = records.stream()
                    .map(CommunicationDO::getCommunicatorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, String> nameMap = userIds.isEmpty()
                    ? new HashMap<>()
                    : userMapper.selectBatchIds(userIds).stream()
                            .collect(Collectors.toMap(UserDO::getId, UserDO::getNickname));
            records.forEach(item -> item.setCommunicatorName(nameMap.get(item.getCommunicatorId())));
        }
        return result;
    }

    @Override
    public boolean createCommunication(CreateCommunicationDTO validator, Integer communicatorId) {
        CommunicationDO communication = CommunicationDO.builder()
                .recommendationId(validator.getRecommendationId())
                .type(validator.getType() == null ? "phone" : validator.getType())
                .content(validator.getContent())
                .communicatorId(communicatorId)
                .communicateTime(new Date())
                .build();
        return this.save(communication);
    }

    @Override
    public boolean deleteCommunication(Integer id) {
        CommunicationDO communication = this.getById(id);
        if (communication == null) {
            throw new NotFoundException(80270);
        }
        return this.removeById(id);
    }
}
