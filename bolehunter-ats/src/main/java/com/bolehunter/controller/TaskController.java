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

import com.bolehunter.common.LocalUser;
import com.bolehunter.dto.task.CreateTaskDTO;
import com.bolehunter.model.TaskDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.TaskService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 任务（工作台待办/到期提醒）
 */
@RestController
@RequestMapping("/recruit/task")
@PermissionModule(value = "任务管理")
@Validated
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 我的待办任务（scope: today/overdue/all）
     */
    @GetMapping("/mine")
    @LoginRequired
    public List<TaskDO> mine(
            @RequestParam(name = "scope", required = false, defaultValue = "all")
            @NotBlank(message = "{candidate.name.not-blank}") String scope) {
        UserDO user = LocalUser.getLocalUser();
        return taskService.getMyTodo(user == null ? null : user.getId(), scope);
    }

    /**
     * 按关联对象查询任务
     */
    @GetMapping("/related")
    @LoginRequired
    public List<TaskDO> related(
            @RequestParam("related_type") @NotBlank(message = "{candidate.name.not-blank}") String relatedType,
            @RequestParam("related_id") @Positive(message = "{id.positive}") Integer relatedId) {
        return taskService.getByRelated(relatedType, relatedId);
    }

    /**
     * 批量统计关联对象的紧急任务数（今日到期/已逾期，看板卡片角标用）
     */
    @GetMapping("/urgent-counts")
    @LoginRequired
    public java.util.Map<Integer, Long> urgentCounts(
            @RequestParam("related_type") @NotBlank(message = "{candidate.name.not-blank}") String relatedType,
            @RequestParam("ids") List<Integer> ids) {
        return taskService.getUrgentCounts(relatedType, ids);
    }

    /**
     * 新增任务
     */
    @Logger(template = "新增任务")
    @PostMapping("")
    @LoginRequired
    public CreatedVO create(@RequestBody @Validated CreateTaskDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        taskService.createTask(validator, user == null ? null : user.getId());
        return new CreatedVO(8820);
    }

    /**
     * 完成任务
     */
    @PutMapping("/{id}/done")
    @LoginRequired
    public UpdatedVO done(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        taskService.doneTask(id);
        return new UpdatedVO(8830);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @LoginRequired
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        taskService.deleteTask(id);
        return new DeletedVO(8840);
    }
}
