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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.FollowDO;
import com.bolehunter.service.FollowService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 关注公司（迭代 31 重构：纯 follow 表数据驱动。
 * 原实现混合了观察者模式反射注册（SingleUtil 内存注册表 + 运行时生成类源码），
 * backend 重启即丢失全部关注关系，且新公司需手动编译生成类——全部删除，
 * 关注关系以 follow 表为唯一事实来源；职位发布通知见 PositionController）。
 */
@Slf4j
@RestController
@RequestMapping("/recruit/follow")
@PermissionModule(value = "关注公司")
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * 添加关注
     */
    @PostMapping("/add")
    @GroupRequired
    @PermissionMeta(value = "新增关注")
    public CreatedVO addFollow(@RequestBody FollowDO followDO) {
        // 查询是否已经关注过该公司
        FollowDO follow = find(followDO.getUserId(), followDO.getCompanyId());
        if (follow != null) {
            // 已经关注过该公司
            throw new NotFoundException(24000);
        }
        // 未关注过该公司，写入 follow 表（职位发布时的通知按本表查询）
        followService.create(followDO);
        return new CreatedVO(1100);
    }

    /**
     * 根据用户id和公司id取消关注该公司
     */
    @DeleteMapping("/cancel")
    @GroupRequired
    @PermissionMeta(value = "取消关注")
    public DeletedVO cancelFollow(@RequestParam @Positive(message = "{id.positive}") Integer userId, @RequestParam @Positive(message = "{id.positive}") Integer companyId) {
        followService.cancelFollow(userId, companyId);
        return new DeletedVO(1200);
    }

    /**
     * 根据用户id查询该用户的所有关注
     */
    @GetMapping("/{userId}")
    @GroupRequired
    @PermissionMeta(value = "查看我的关注")
    public List<CompanyDO> getAllByUserId(@PathVariable(value = "userId") @Positive(message = "{id.positive}") Integer userId) {
        return followService.getAllByUserId(userId);
    }

    /**
     * 根据用户id和公司id查询关注，避免重复关注同一公司
     */
    @GetMapping("/find")
    public FollowDO find(@RequestParam Integer userId, @RequestParam Integer companyId) {
        QueryWrapper<FollowDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("company_id", companyId);
        return followService.getOne(wrapper, false);
    }
}
