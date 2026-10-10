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

import com.bolehunter.dto.flag.CreateOrUpdateFeatureFlagDTO;
import com.bolehunter.model.FeatureFlagDO;
import com.bolehunter.service.FeatureFlagService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * 功能开关管理
 */
@RestController
@RequestMapping("/recruit/flag")
@PermissionModule(value = "功能开关")
@Validated
public class FeatureFlagController {

    @Autowired
    private FeatureFlagService featureFlagService;

    /**
     * 全部功能开关
     */
    @Logger(template = "查询功能开关")
    @GetMapping("/list")
    @PermissionMeta(value = "查询功能开关")
    public List<FeatureFlagDO> list() {
        return featureFlagService.getAllFlags();
    }

    /**
     * 新增功能开关
     */
    @Logger(template = "新增功能开关")
    @PostMapping("")
    @PermissionMeta(value = "新增功能开关")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateFeatureFlagDTO validator) {
        featureFlagService.createFlag(validator);
        return new CreatedVO(1);
    }

    /**
     * 更新功能开关
     */
    @Logger(template = "更新功能开关")
    @PutMapping("/{id}")
    @PermissionMeta(value = "更新功能开关")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id,
                            @RequestBody @Validated CreateOrUpdateFeatureFlagDTO validator) {
        FeatureFlagDO flag = featureFlagService.getById(id);
        if (flag == null) {
            throw new NotFoundException(80130);
        }
        featureFlagService.updateFlag(flag, validator);
        return new UpdatedVO(8200);
    }

    /**
     * 切换开关状态
     */
    @Logger(template = "切换功能开关状态")
    @PutMapping("/{id}/toggle")
    @PermissionMeta(value = "更新功能开关")
    public Map<String, Object> toggle(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        int enabled = featureFlagService.toggleFlag(id);
        return java.util.Collections.singletonMap("enabled", enabled);
    }

    /**
     * 删除功能开关
     */
    @Logger(template = "删除功能开关")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除功能开关")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        FeatureFlagDO flag = featureFlagService.getById(id);
        if (flag == null) {
            throw new NotFoundException(80130);
        }
        featureFlagService.removeById(id);
        return new DeletedVO(3);
    }
}
