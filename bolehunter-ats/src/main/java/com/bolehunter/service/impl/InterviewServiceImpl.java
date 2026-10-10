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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.mapper.InterviewMapper;
import com.bolehunter.model.InterviewDO;
import com.bolehunter.model.result.ApplicationResultDO;
import com.bolehunter.model.result.InterviewResultDO;
import com.bolehunter.service.InterviewService;
import com.bolehunter.vo.PageResponseVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


/**
 * @author Eval
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, InterviewDO> implements InterviewService {

    @Resource
    private InterviewMapper interviewMapper;

    @Override
    public PageResponseVO<InterviewResultDO> getByHrId(Integer count, Integer page, Integer hrID) {
        Page<InterviewResultDO> pager = new Page<>(page, count);
        IPage<InterviewResultDO> paging = interviewMapper.getByHrId(pager, hrID);
        return PageUtil.build(paging);
    }

    @Override
    public PageResponseVO<InterviewResultDO> getByUserId(Integer count, Integer page, Integer userId) {
        Page<InterviewResultDO> pager = new Page<>(page, count);
        IPage<InterviewResultDO> paging = interviewMapper.getByUserId(pager, userId);
        return PageUtil.build(paging);
    }
}
