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
package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.UserIdentityDO;

public interface UserIdentityService extends IService<UserIdentityDO> {

    /**
     * 新建用户认证信息
     *
     * @param userId       用户id
     * @param identityType 认证类型
     * @param identifier   标识
     * @param credential   凭证
     * @return 用户认证
     */
    UserIdentityDO createIdentity(Integer userId,
                                  String identityType,
                                  String identifier,
                                  String credential);

    /**
     * 新建用户认证信息
     *
     * @param userIdentity 用户认证信息
     * @return 用户认证
     */
    UserIdentityDO createIdentity(UserIdentityDO userIdentity);

    /**
     * 新建用户认证信息 (USERNAME_PASSWORD)
     *
     * @param userId   用户id
     * @param username 用户名
     * @param password 密码
     * @return 用户认证
     */
    UserIdentityDO createUsernamePasswordIdentity(Integer userId,
                                                  String username,
                                                  String password);


    /**
     * 验证用户认证信息 (USERNAME_PASSWORD)
     *
     * @param userId   用户id
     * @param username 用户名
     * @param password 密码
     * @return 是否验证成功
     */
    boolean verifyUsernamePassword(Integer userId, String username, String password);

    /**
     * 修改密码
     *
     * @param userId   用户id
     * @param password 新密码
     * @return 是否成功
     */
    boolean changePassword(Integer userId, String password);

    /**
     * 修改用户名
     *
     * @param userId   用户id
     * @param username 新用户名
     * @return 是否成功
     */
    boolean changeUsername(Integer userId, String username);

    /**
     * 修改用户名密码
     *
     * @param userId   用户id
     * @param username 新用户名
     * @param password 新密码
     * @return 是否成功
     */
    boolean changeUsernamePassword(Integer userId, String username, String password);
}
