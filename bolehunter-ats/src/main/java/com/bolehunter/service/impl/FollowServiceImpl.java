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
import com.bolehunter.dto.follow.CreateFollowDTO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.FollowDO;
import com.bolehunter.mapper.FollowMapper;
import com.bolehunter.service.FollowService;
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
 * @since 2022-03-31
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, FollowDO> implements FollowService {
    @Resource
    private FollowMapper followMapper;

    @Override
    public boolean addFollow(CreateFollowDTO validator) {
        FollowDO followDO = new FollowDO();
        followDO.setCompanyId(validator.getCompanyId());
        followDO.setUserId(validator.getUserId());
        return followMapper.insert(followDO) > 0;
    }

    @Override
    public FollowDO getById(Integer id) {
        FollowDO follow = followMapper.selectById(id);
        return follow;
    }

    @Override
    public boolean deleteById(Integer id) {
        return followMapper.deleteById(id) > 0;
    }

    @Override
    public List<CompanyDO> getAllByUserId(Integer userId) {
        return followMapper.getByUserId(userId);
    }

    @Override
    public boolean cancelFollow(Integer userId, Integer companyId) {
        // 根据用户id和公司id查询关注信息
        FollowDO follow = followMapper.getByUserIdAndCompanyId(userId, companyId);
        if (follow == null) {
            throw new NotFoundException(23000);
        }
        // 删除该关注
        return followMapper.deleteById(follow.getId()) > 0;
    }

    @Override
    public boolean create(FollowDO followDO) {
        return followMapper.insert(followDO) > 0;
    }
}
