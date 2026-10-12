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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public Map<Integer, Long> getUrgentCounts(String relatedType, List<Integer> relatedIds) {
        Map<Integer, Long> result = new HashMap<>();
        if (relatedIds == null || relatedIds.isEmpty()) {
            return result;
        }
        // 防御：relatedType 白名单校验（拼入 SQL 前确保安全）
        if (!"recommendation".equals(relatedType) && !"client".equals(relatedType) && !"candidate".equals(relatedType)) {
            return result;
        }
        List<Integer> ids = relatedIds.stream().filter(id -> id != null && id > 0).distinct().collect(java.util.stream.Collectors.toList());
        if (ids.isEmpty()) {
            return result;
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            in.append(i == 0 ? "" : ",").append(ids.get(i));
        }
        // 时间窗口用 MySQL NOW() 计算，避免 JVM 与数据库时区不一致
        // 紧急 = 已逾期（due < NOW）或今日到期（due < 明天 0 点），二者可合并为 due < 明天 0 点
        String sql = "SELECT related_id, COUNT(*) AS cnt FROM task "
                + "WHERE status = 0 AND delete_time IS NULL AND related_type = '" + relatedType + "' "
                + "AND related_id IN (" + in + ") "
                + "AND due_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY) "
                + "GROUP BY related_id";
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getInt("related_id"), rs.getLong("cnt"));
        });
        return result;
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
