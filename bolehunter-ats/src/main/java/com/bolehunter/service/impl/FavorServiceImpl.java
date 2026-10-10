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

import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.model.FavorDO;
import com.bolehunter.mapper.FavorMapper;
import com.bolehunter.model.PositionDO;
import com.bolehunter.service.FavorService;
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
 * @since 2022-04-02
 */
@Service
public class FavorServiceImpl extends ServiceImpl<FavorMapper, FavorDO> implements FavorService {

    @Resource
    private FavorMapper favorMapper;

    @Override
    public List<PositionDO> getAllByUserId(Integer userId) {
        return favorMapper.getByUserId(userId);
    }

    @Override
    public boolean cancelFavor(Integer userId, Integer positionId) {
        // 根据用户id和职位id查询收藏信息
        FavorDO favor = favorMapper.getByUserIdAndFavorId(userId, positionId);
        if (favor == null) {
            throw new NotFoundException(45000);
        }
        // 删除该收藏
        return favorMapper.deleteById(favor.getId()) > 0;
    }

    @Override
    public boolean create(FavorDO favorDO) {
        return favorMapper.insert(favorDO) > 0;
    }
}
