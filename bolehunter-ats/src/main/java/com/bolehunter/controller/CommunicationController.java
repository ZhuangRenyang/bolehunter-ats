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
package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.LocalUser;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.communication.CreateCommunicationDTO;
import com.bolehunter.model.CommunicationDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.CommunicationService;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * 沟通记录（顾问与客户 HR 沟通的逐次代录）
 */
@RestController
@RequestMapping("/recruit/communication")
@PermissionModule(value = "沟通记录")
@Validated
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    /**
     * 按推荐记录分页查询沟通记录（时间倒序）
     */
    @GetMapping("/page")
    @LoginRequired
    public PageResponseVO<CommunicationDO> page(
            @RequestParam("recommendation_id") @Positive(message = "{id.positive}") Integer recommendationId,
            @RequestParam(name = "count", required = false, defaultValue = "20")
            @jakarta.validation.constraints.Max(value = 50, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @jakarta.validation.constraints.Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<CommunicationDO> iPage = communicationService.getByRecommendation(recommendationId, page, count);
        return PageUtil.build(iPage);
    }

    /**
     * 新增沟通记录
     */
    @Logger(template = "新增沟通记录")
    @PostMapping("")
    @LoginRequired
    public CreatedVO create(@RequestBody @Validated CreateCommunicationDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        communicationService.createCommunication(validator, user == null ? null : user.getId());
        return new CreatedVO(8800);
    }

    /**
     * 删除沟通记录
     */
    @DeleteMapping("/{id}")
    @LoginRequired
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        communicationService.deleteCommunication(id);
        return new DeletedVO(8810);
    }
}
