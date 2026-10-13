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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.mapper.LogMapper;
import com.bolehunter.model.LogDO;
import com.bolehunter.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, LogDO> implements LogService {

    @Override
    public IPage<LogDO> getLogPage(Integer page, Integer count, String name, Integer statusCode, Date start, Date end) {
        Page<LogDO> pager = new Page<>(page, count);
        LambdaQueryWrapper<LogDO> lq = new LambdaQueryWrapper<>();
        // 操作人改模糊匹配：审计页通常只记得半个名字
        if (!StringUtils.isEmpty(name)) {
            lq.like(LogDO::getUsername, name);
        }
        // 状态码筛选：快速定位失败请求（如 407/500）
        if (statusCode != null) {
            lq.eq(LogDO::getStatusCode, statusCode);
        }
        if (start != null) {
            lq.ge(LogDO::getCreateTime, start);
        }
        if (end != null) {
            lq.le(LogDO::getCreateTime, end);
        }
        lq.orderByDesc(LogDO::getCreateTime);
        return this.baseMapper.selectPage(pager, lq);
    }

    @Override
    public IPage<LogDO> searchLogPage(Integer page, Integer count, String name, String keyword, Date start, Date end) {
        Page<LogDO> pager = new Page<>(page, count);
        IPage<LogDO> iPage = this.baseMapper.searchLogsByUsernameAndKeywordAndRange(pager, name, "%" + keyword + "%", start, end);
        return iPage;
    }

    @Override
    public IPage<String> getUserNamePage(Integer page, Integer count) {
        Page<LogDO> pager = new Page<>(page, count);
        IPage<String> iPage = this.baseMapper.getUserNames(pager);
        return iPage;
    }

    @Override
    public boolean createLog(String message, String permission, Integer userId, String username, String method, String path, Integer status) {
        LogDO record = LogDO.builder()
                .message(message)
                .userId(userId)
                .username(username)
                .statusCode(status)
                .method(method)
                .path(path)
                .build();
        if (permission != null) {
            record.setPermission(permission);
        }
        record.setCreateTime(new Date());
        return this.baseMapper.insert(record) > 0;
    }
}
