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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.model.ApplicationDO;
import com.bolehunter.mapper.ApplicationMapper;
import com.bolehunter.model.result.ApplicationResultDO;
import com.bolehunter.service.ApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.vo.PageResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-04
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, ApplicationDO> implements ApplicationService {

    @Resource
    private ApplicationMapper applicationMapper;

    @Override
    public boolean create(ApplicationDO applicationDO) {
        return applicationMapper.insert(applicationDO) > 0;
    }

    @Override
    public PageResponseVO<ApplicationResultDO> getByHrId(Integer count, Integer page, Integer hrID, Integer state) {
        Page<ApplicationResultDO> pager = new Page<>(page, count);
        IPage<ApplicationResultDO> paging = applicationMapper.getByHrId(pager, hrID, state);
        return PageUtil.build(paging);
    }

    @Override
    public boolean updateState(Integer id, Integer state) {
        return applicationMapper.updateState(id, state);
    }

    @Override
    public PageResponseVO<ApplicationResultDO> sort(Integer count, Integer page, Integer hrID) {
        Page<ApplicationResultDO> pager = new Page<>(page, count);
        IPage<ApplicationResultDO> paging = applicationMapper.sortByGrade(pager, hrID);
        return PageUtil.build(paging);
    }

    @Override
    public PageResponseVO<ApplicationResultDO> getByUserId(Integer count, Integer page, Integer userId) {
        Page<ApplicationResultDO> pager = new Page<>(page, count);
        IPage<ApplicationResultDO> paging = applicationMapper.getByUserId(pager, userId);
        return PageUtil.build(paging);
    }

}
