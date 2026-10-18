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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.model.CandidateDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件中心（迭代 35）：汇总已上传简历文件的候选人清单。
 * 文件实体存储在对象存储（七牛），本服务只做索引视图。
 */
@Slf4j
@Service
public class FileCenterService {

    @Autowired
    private CandidateMapper candidateMapper;

    /** 已上传简历文件的候选人清单（按更新时间倒序） */
    public List<Map<String, Object>> listResumeFiles() {
        QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("resume_file").ne("resume_file", "")
            .orderByDesc("update_time");
        return candidateMapper.selectList(wrapper).stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("candidateId", c.getId());
            m.put("name", c.getName());
            m.put("phone", c.getPhone());
            m.put("file", c.getResumeFile());
            m.put("time", c.getUpdateTime());
            return m;
        }).toList();
    }
}
