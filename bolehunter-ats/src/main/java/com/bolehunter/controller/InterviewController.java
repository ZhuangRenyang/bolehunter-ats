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
import com.bolehunter.model.InterviewDO;
import com.bolehunter.model.PositionDO;
import com.bolehunter.model.result.InterviewResultDO;
import com.bolehunter.service.InterviewService;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * @author Eval
 * @since 2022-04-16
 */
@RestController
@RequestMapping("/recruit/interview")
@PermissionModule(value = "面试")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @GetMapping("/page/{hrID}")
    @GroupRequired
    @PermissionMeta(value = "面试管理")
    public PageResponseVO<InterviewResultDO> page(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page,
            @PathVariable(value = "hrID") @Positive(message = "{id.positive}") Integer hrID,
            @RequestParam Integer state
    ) {
        return interviewService.getByHrId(count, page, hrID);
    }

    @GetMapping("/page/find/{userId}")
    @GroupRequired
    @PermissionMeta(value = "我的面试")
    public PageResponseVO<InterviewResultDO> pageByUserId(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page,
            @PathVariable(value = "userId") @Positive(message = "{id.positive}") Integer userId,
            @RequestParam Integer state
    ) {
        return interviewService.getByUserId(count, page, userId);
    }

    @PostMapping("/sendInterviewNotify")
    @GroupRequired
    @PermissionMeta(value = "面试管理")
    public UpdatedVO sendInterviewNotify(@RequestBody InterviewDO interviewDO){
        QueryWrapper<InterviewDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", interviewDO.getId());
        InterviewDO result = interviewService.getOne(wrapper);
        if (result == null) {
            throw new NotFoundException(41000);
        }
        result.setStatus(1);
        result.setAddress(interviewDO.getAddress());
        result.setMemo(interviewDO.getMemo());
        result.setTime(interviewDO.getTime());
        interviewService.updateById(result);
        return new UpdatedVO(4100);
    }

    @PostMapping("/updateInterviewResult")
    @GroupRequired
    @PermissionMeta(value = "面试管理")
    public UpdatedVO updateInterviewResult(@RequestBody InterviewDO interviewDO){
        QueryWrapper<InterviewDO> wrapper = new QueryWrapper<>();
        wrapper.eq("id", interviewDO.getId());
        InterviewDO result = interviewService.getOne(wrapper);
        if (result == null) {
            throw new NotFoundException(41000);
        }
        result.setStatus(interviewDO.getStatus());
        result.setComments(interviewDO.getComments());
        interviewService.updateById(result);
        return new UpdatedVO(4100);
    }
}
