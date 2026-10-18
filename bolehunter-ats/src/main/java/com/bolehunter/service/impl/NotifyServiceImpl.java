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
import com.bolehunter.core.websocket.WsPayload;
import com.bolehunter.core.websocket.WsSessionManager;
import com.bolehunter.model.NotifyDO;
import com.bolehunter.mapper.NotifyMapper;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.NotifyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-16
 */
@Slf4j
@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, NotifyDO> implements NotifyService {

    @Resource
    private NotifyMapper notifyMapper;

    @Resource
    private UserService userService;

    /**
     * WebSocket 会话注册表：用于新通知的实时送达（迭代23）。
     * 推送失败绝不影响通知本身的写入，因此这里用可选注入 + 全异常兜底。
     */
    @Resource
    private WsSessionManager wsSessionManager;

    @Override
    public boolean create(NotifyDO notifyDO) {
        boolean saved = notifyMapper.insert(notifyDO) > 0;
        if (saved) {
            pushRealtime(notifyDO);
        }
        return saved;
    }

    /**
     * 通知入库后实时推送给在线当事人；对方离线则静默跳过（其上线后仍可拉取列表）。
     */
    private void pushRealtime(NotifyDO notifyDO) {
        try {
            if (notifyDO == null || notifyDO.getUserName() == null) {
                return;
            }
            UserDO target = userService.getUserByUsername(notifyDO.getUserName());
            if (target == null) {
                return;
            }
            int delivered = wsSessionManager.push(target.getId(),
                WsPayload.of(WsPayload.Type.NOTICE, toView(notifyDO)));
            if (delivered > 0) {
                log.info("[notify] 实时推送 {} 条给 uid={}", delivered, target.getId());
            }
        } catch (Exception e) {
            // 通知已落库，推送是锦上添花；任何异常都不得回吐给调用方
            log.warn("[notify] 实时推送失败，已入库不影响：{}", e.getMessage());
        }
    }

    /**
     * 构造下行视图：BaseModel 时间字段 @JsonIgnore，故显式输出各字段
     */
    private static Map<String, Object> toView(NotifyDO n) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", n.getId());
        view.put("content", n.getContent());
        view.put("userName", n.getUserName());
        view.put("isRead", n.getIsRead());
        view.put("time", n.getTime() == null ? null : n.getTime().getTime());
        return view;
    }

    /**
     * 迭代 29 修复：原实现把前端传入的用户名（username）误当作昵称（nickname）去查用户，
     * 昵称查不到时 userDO 为 null 直接空指针（生产 500 白屏级问题）。
     * notify 表的 user_name 列存的就是登录用户名，直接按列查询即可。
     */
    @Override
    public List<NotifyDO> get(String userName, String isRead) {
        QueryWrapper<NotifyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName).eq("is_read", isRead);
        return notifyMapper.selectList(wrapper);
    }

    @Override
    public List<NotifyDO> getAll(String userName) {
        QueryWrapper<NotifyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        return notifyMapper.selectList(wrapper);
    }

    @Override
    public boolean update(String userName) {
        return notifyMapper.update(userName);
    }
}
