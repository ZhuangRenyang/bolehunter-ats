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
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.dto.task.CreateTaskDTO;
import com.bolehunter.mapper.TaskMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.TaskDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.TaskService;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TaskDO> implements TaskService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean createTask(CreateTaskDTO validator, Integer ownerId) {
        TaskDO task = TaskDO.builder()
                .title(validator.getTitle())
                .content(validator.getContent())
                .dueTime(validator.getDueTime())
                .ownerId(ownerId)
                .relatedType(validator.getRelatedType())
                .relatedId(validator.getRelatedId())
                .status(0)
                .build();
        return this.save(task);
    }

    @Override
    public boolean doneTask(Integer id) {
        TaskDO task = this.getById(id);
        if (task == null) {
            throw new NotFoundException(80280);
        }
        TaskDO patch = new TaskDO();
        patch.setId(id);
        patch.setStatus(1);
        patch.setDoneTime(new Date());
        return this.updateById(patch);
    }

    @Override
    public boolean deleteTask(Integer id) {
        TaskDO task = this.getById(id);
        if (task == null) {
            throw new NotFoundException(80280);
        }
        return this.removeById(id);
    }

    @Override
    public List<TaskDO> getByRelated(String relatedType, Integer relatedId) {
        QueryWrapper<TaskDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(TaskDO::getRelatedType, relatedType)
                .eq(TaskDO::getRelatedId, relatedId)
                .orderByAsc(TaskDO::getDueTime)
                .last("LIMIT 100");
        List<TaskDO> tasks = this.list(wrapper);
        if (!tasks.isEmpty()) {
            Map<Integer, String> nameMap = userMapper.selectBatchIds(tasks.stream()
                            .map(TaskDO::getOwnerId).filter(id -> id != null).distinct()
                            .collect(java.util.stream.Collectors.toList()))
                    .stream().collect(java.util.stream.Collectors.toMap(UserDO::getId, UserDO::getNickname));
            tasks.forEach(task -> task.setOwnerName(nameMap.get(task.getOwnerId())));
        }
        return tasks;
    }

    @Override
    public List<TaskDO> getMyTodo(Integer ownerId, String scope) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrowStart = calendar.getTime();

        QueryWrapper<TaskDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(TaskDO::getOwnerId, ownerId)
                .eq(TaskDO::getStatus, 0)
                .orderByAsc(TaskDO::getDueTime)
                .last("LIMIT 100");
        if ("today".equals(scope)) {
            // 今日到期：今天 0 点 ~ 明天 0 点
            wrapper.lambda().ge(TaskDO::getDueTime, todayStart)
                    .lt(TaskDO::getDueTime, tomorrowStart);
        } else if ("overdue".equals(scope)) {
            // 已逾期：今天 0 点之前仍未完成
            wrapper.lambda().lt(TaskDO::getDueTime, todayStart);
        }
        List<TaskDO> tasks = this.list(wrapper);
        if (!tasks.isEmpty()) {
            Map<Integer, String> nameMap = userMapper.selectBatchIds(tasks.stream()
                            .map(TaskDO::getOwnerId).filter(id -> id != null).distinct()
                            .collect(java.util.stream.Collectors.toList()))
                    .stream().collect(java.util.stream.Collectors.toMap(UserDO::getId, UserDO::getNickname));
            tasks.forEach(task -> task.setOwnerName(nameMap.get(task.getOwnerId())));
        }
        return tasks;
    }
}
