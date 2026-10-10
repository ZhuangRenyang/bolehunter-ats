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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.mapper.ExperienceMapper;
import com.bolehunter.mapper.ProjectMapper;
import com.bolehunter.model.ExperienceDO;
import com.bolehunter.model.ProjectDO;
import com.bolehunter.service.ExperienceService;
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
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, ExperienceDO> implements ExperienceService {

    @Resource
    private ExperienceMapper experienceMapper;

    @Override
    public List<ExperienceDO> getByResumeId(Integer rid) {
        QueryWrapper<ExperienceDO> experienceDOQueryWrapper=new QueryWrapper<>();
        experienceDOQueryWrapper.lambda().eq(ExperienceDO::getResumeId,rid);
        List<ExperienceDO>  experienceDOS = experienceMapper.selectList(experienceDOQueryWrapper);
        return experienceDOS;
    }
}
