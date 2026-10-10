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

import com.bolehunter.dto.tag.CreateOrUpdateTagDTO;
import com.bolehunter.model.TagDO;
import com.bolehunter.service.TagService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 标签管理
 */
@RestController
@RequestMapping("/recruit/tag")
@PermissionModule(value = "标签管理")
@Validated
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 全部标签（下拉/筛选用，仅需登录）
     */
    @GetMapping("/list")
    @LoginRequired
    public List<TagDO> list() {
        return tagService.getAllTags();
    }

    /**
     * 新增标签
     */
    @Logger(template = "新增标签")
    @PostMapping("")
    @PermissionMeta(value = "维护标签")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateTagDTO validator) {
        tagService.createTag(validator.getName());
        return new CreatedVO(8600);
    }

    /**
     * 更新标签
     */
    @Logger(template = "更新标签")
    @PutMapping("/{id}")
    @PermissionMeta(value = "维护标签")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id,
                            @RequestBody @Validated CreateOrUpdateTagDTO validator) {
        TagDO tag = tagService.getById(id);
        if (tag == null) {
            throw new NotFoundException(80210);
        }
        tagService.updateTag(tag, validator.getName());
        return new UpdatedVO(8610);
    }

    /**
     * 删除标签
     */
    @Logger(template = "删除标签")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "维护标签")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        tagService.deleteTag(id);
        return new DeletedVO(8620);
    }
}
