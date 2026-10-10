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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.core.util.EncryptUtil;
import com.bolehunter.common.constant.IdentityConstant;
import com.bolehunter.mapper.UserIdentityMapper;
import com.bolehunter.model.UserIdentityDO;
import com.bolehunter.service.UserIdentityService;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityServiceImpl extends ServiceImpl<UserIdentityMapper, UserIdentityDO> implements UserIdentityService {


    @Override
    public UserIdentityDO createIdentity(Integer userId, String identityType, String identifier, String credential) {
        UserIdentityDO userIdentity = new UserIdentityDO();
        userIdentity.setUserId(userId);
        userIdentity.setIdentityType(identityType);
        userIdentity.setIdentifier(identifier);
        userIdentity.setCredential(credential);
        return this.createIdentity(userIdentity);
    }

    @Override
    public UserIdentityDO createIdentity(UserIdentityDO userIdentity) {
        this.baseMapper.insert(userIdentity);
        return userIdentity;
    }

    @Override
    public UserIdentityDO createUsernamePasswordIdentity(Integer userId, String identifier, String credential) {
        // 密码加密
        credential = EncryptUtil.encrypt(credential);
        return this.createIdentity(userId, IdentityConstant.USERNAME_PASSWORD_IDENTITY, identifier, credential);
    }

    @Override
    public boolean verifyUsernamePassword(Integer userId, String username, String password) {
        QueryWrapper<UserIdentityDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserIdentityDO::getUserId, userId)
                .eq(UserIdentityDO::getIdentityType, IdentityConstant.USERNAME_PASSWORD_IDENTITY)
                .eq(UserIdentityDO::getIdentifier, username);
        UserIdentityDO userIdentity = this.baseMapper.selectOne(wrapper);
        return EncryptUtil.verify(userIdentity.getCredential(), password);
    }

    @Override
    public boolean changePassword(Integer userId, String password) {
        String encrypted = EncryptUtil.encrypt(password);
        UserIdentityDO userIdentity = UserIdentityDO.builder().credential(encrypted).build();
        QueryWrapper<UserIdentityDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserIdentityDO::getUserId, userId);
        return this.baseMapper.update(userIdentity, wrapper) > 0;
    }

    @Override
    public boolean changeUsername(Integer userId, String username) {
        UserIdentityDO userIdentity = UserIdentityDO.builder().identifier(username).build();
        QueryWrapper<UserIdentityDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserIdentityDO::getUserId, userId);
        return this.baseMapper.update(userIdentity, wrapper) > 0;
    }

    @Override
    public boolean changeUsernamePassword(Integer userId, String username, String password) {
        UserIdentityDO userIdentity =
                UserIdentityDO.builder().identifier(username).credential(password).build();
        QueryWrapper<UserIdentityDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UserIdentityDO::getUserId, userId);
        return this.baseMapper.update(userIdentity, wrapper) > 0;
    }
}
