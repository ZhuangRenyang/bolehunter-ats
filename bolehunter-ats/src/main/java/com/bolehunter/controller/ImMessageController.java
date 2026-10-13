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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bolehunter.common.LocalUser;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.ImMessageService;
import com.bolehunter.vo.UpdatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部即时消息（迭代23）
 * <p>WebSocket 负责实时送达（{@code /recruit/ws}），本组 REST 接口负责
 * 历史回看、会话列表、未读数与已读标记，也是 WebSocket 不可用时的降级通道。</p>
 */
@RestController
@RequestMapping("/recruit/im")
@PermissionModule(value = "实时消息")
@Validated
public class ImMessageController {

    @Autowired
    private ImMessageService imMessageService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 会话列表（含对方昵称、最后一条消息、未读数）
     */
    @GetMapping("/conversations")
    @LoginRequired
    public List<Map<String, Object>> conversations(@RequestParam(defaultValue = "50") Integer limit) {
        return imMessageService.conversations(currentUserId(), limit);
    }

    /**
     * 与某人的聊天历史
     */
    @GetMapping("/history")
    @LoginRequired
    public Map<String, Object> history(@RequestParam Integer peerId,
                                       @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("peerId", peerId);
        result.put("messages", imMessageService.history(currentUserId(), peerId, limit));
        return result;
    }

    /**
     * 我的未读消息总数
     */
    @GetMapping("/unread")
    @LoginRequired
    public Map<String, Object> unreadTotal() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadTotal", imMessageService.unreadTotal(currentUserId()));
        // 顺带回传我方用户 id：前端据此判断气泡左右侧，无需再请求一次用户信息接口
        result.put("me", currentUserId());
        return result;
    }

    /**
     * 把来自 peerId 的消息标记为已读（并通过 WebSocket 回执给对方）
     */
    @PutMapping("/read")
    @LoginRequired
    public UpdatedVO markRead(@RequestParam Integer peerId) {
        imMessageService.markRead(currentUserId(), peerId);
        return new UpdatedVO();
    }

    /**
     * 可发起会话的同事（排除自己）
     */
    @GetMapping("/colleagues")
    @LoginRequired
    public List<Map<String, Object>> colleagues() {
        Integer me = currentUserId();
        List<UserDO> users = userMapper.selectList(Wrappers.<UserDO>lambdaQuery().ne(UserDO::getId, me));
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDO u : users) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname());
            list.add(item);
        }
        return list;
    }

    /**
     * 发送消息（HTTP 通道）。WebSocket 不可用时的降级路径，行为与 WS 发消息一致。
     */
    @Logger(template = "发送内部消息")
    @PostMapping("/send")
    @LoginRequired
    public Map<String, Object> send(@RequestBody Map<String, Object> body) {
        Integer receiverId = body == null ? null : toInt(body.get("receiverId"));
        String content = body == null ? null : String.valueOf(body.get("content"));
        imMessageService.send(currentUserId(), receiverId, content);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("receiverId", receiverId);
        result.put("sent", true);
        return result;
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private static Integer currentUserId() {
        UserDO user = LocalUser.getLocalUser();
        return user == null ? null : user.getId();
    }
}
