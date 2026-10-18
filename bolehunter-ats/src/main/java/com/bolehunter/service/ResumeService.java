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

import com.bolehunter.dto.resume.CreateOrUpdateResumeDTO;
import com.bolehunter.model.ResumeDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.vo.ResumeVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ZhuangRenyang
 * @since 2022-03-14
 */
public interface ResumeService extends IService<ResumeDO> {

    //获取用户简历信息
    ResumeVO getByUserId(Integer userId);

    boolean createResume(CreateOrUpdateResumeDTO validator);

    boolean updateResume(ResumeDO resumeDO, CreateOrUpdateResumeDTO validator);

    ResumeDO getSimpleByUserId(Integer userId);
}
