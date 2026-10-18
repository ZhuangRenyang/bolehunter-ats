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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bolehunter.common.LocalUser;
import com.bolehunter.core.annotation.AdminRequired;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.model.FeedbackDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.FeedbackService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.UpdatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 意见反馈（迭代 34）：登录用户提交，管理员查看与回复。
 */
@RestController
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交反馈（登录用户）
     */
    @PostMapping("/recruit/feedback")
    @LoginRequired
    public CreatedVO create(@RequestBody Map<String, String> body) {
        UserDO user = LocalUser.getLocalUser();
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            throw new com.bolehunter.core.exception.ParameterException(10030);
        }
        FeedbackDO feedback = new FeedbackDO();
        if (user != null) {
            feedback.setUserId(user.getId());
            feedback.setUsername(user.getUsername());
        }
        feedback.setContent(content.trim().substring(0, Math.min(2000, content.trim().length())));
        feedback.setStatus(0);
        feedbackService.save(feedback);
        return new CreatedVO();
    }

    /**
     * 反馈分页（管理员）
     */
    @GetMapping("/recruit/admin/feedback/page")
    @AdminRequired
    public IPage<FeedbackDO> page(
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(name = "count", required = false, defaultValue = "10") Integer count,
            @RequestParam(name = "status", required = false) Integer status) {
        QueryWrapper<FeedbackDO> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return feedbackService.page(new Page<>(page, count), wrapper);
    }

    /**
     * 反馈回复/标记已处理（管理员）
     */
    @PutMapping("/recruit/admin/feedback/{id}")
    @AdminRequired
    public UpdatedVO reply(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        FeedbackDO feedback = feedbackService.getById(id);
        if (feedback != null) {
            feedback.setStatus(1);
            feedback.setReply(body.get("reply"));
            feedbackService.updateById(feedback);
        }
        return new UpdatedVO();
    }
}
