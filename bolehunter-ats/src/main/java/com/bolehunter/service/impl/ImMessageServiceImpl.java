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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.websocket.WsPayload;
import com.bolehunter.core.websocket.WsSessionManager;
import com.bolehunter.mapper.ImMessageMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.ImMessageDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.FeatureFlagService;
import com.bolehunter.service.ImMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 内部即时消息实现（迭代23）
 * <p>落库 + 实时送达双写：消息先确保持久化，再尝试 WebSocket 推送；
 * 接收者离线不影响消息入库，其上线后由前端拉取会话列表补上。</p>
 */
@Service
public class ImMessageServiceImpl extends ServiceImpl<ImMessageMapper, ImMessageDO> implements ImMessageService {

    /** 功能开关键：关闭时不接受发送，也不建立推送 */
    public static final String FLAG_KEY = "im_realtime";

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    private static final int MAX_CONTENT = 2000;
    private static final int DEFAULT_LIMIT = 100;

    @Resource
    private ImMessageMapper imMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FeatureFlagService featureFlagService;

    @Resource
    private WsSessionManager wsSessionManager;

    @Override
    public ImMessageDO send(Integer senderId, Integer receiverId, String content) {
        if (!featureFlagService.isEnabled(FLAG_KEY)) {
            throw new ForbiddenException(8080);
        }
        if (content == null || content.isBlank()) {
            throw new ForbiddenException(8081);
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT) {
            throw new ForbiddenException(8081);
        }
        if (Objects.equals(senderId, receiverId)) {
            throw new ForbiddenException(8083);
        }
        UserDO receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new ForbiddenException(8082);
        }

        ImMessageDO msg = new ImMessageDO()
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setContent(text)
            .setIsRead(0);
        imMessageMapper.insert(msg);

        // 先落库再推送：保证推送失败也不会丢消息
        int delivered = wsSessionManager.push(receiverId, WsPayload.of(WsPayload.Type.CHAT, toView(msg)));
        log.info("[im] {} -> {} 送达 {} 个在线端", senderId, receiverId, delivered);
        return msg;
    }

    @Override
    public List<Map<String, Object>> history(Integer me, Integer peer, Integer limit) {
        int size = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, DEFAULT_LIMIT);
        List<ImMessageDO> list = imMessageMapper.selectConversation(me, peer, size);
        return list.stream().map(this::toView).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> conversations(Integer me, Integer limit) {
        int size = (limit == null || limit <= 0) ? 50 : Math.min(limit, 100);
        List<ImMessageDO> lasts = imMessageMapper.selectLastMessageEach(me, size);

        // 未读数按发送者分组一次查完（SQL 聚合，避免逐会话 count）
        Map<Integer, Integer> unreadMap = new LinkedHashMap<>();
        QueryWrapper<ImMessageDO> unreadWrapper = Wrappers.query();
        unreadWrapper.select("sender_id", "COUNT(*) AS cnt")
            .eq("receiver_id", me)
            .eq("is_read", 0)
            .groupBy("sender_id");
        for (Map<String, Object> row : imMessageMapper.selectMaps(unreadWrapper)) {
            Object sid = row.get("sender_id");
            Object cnt = row.get("cnt");
            if (sid != null && cnt != null) {
                unreadMap.put(Integer.valueOf(String.valueOf(sid)), Integer.valueOf(String.valueOf(cnt)));
            }
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ImMessageDO m : lasts) {
            Integer peerId = Objects.equals(m.getSenderId(), me) ? m.getReceiverId() : m.getSenderId();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("peerId", peerId);
            item.put("peerName", displayName(peerId));
            item.put("lastContent", m.getContent());
            item.put("lastTime", formatTime(m.getCreateTime()));
            item.put("outgoing", Objects.equals(m.getSenderId(), me));
            item.put("unreadCount", unreadMap.getOrDefault(peerId, 0));
            result.add(item);
        }
        return result;
    }

    @Override
    public int unreadTotal(Integer me) {
        Long count = lambdaQuery()
            .eq(ImMessageDO::getReceiverId, me)
            .eq(ImMessageDO::getIsRead, 0)
            .count();
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int markRead(Integer me, Integer peer) {
        ImMessageDO patch = new ImMessageDO();
        patch.setIsRead(1);
        patch.setReadTime(new java.util.Date());
        int affected = imMessageMapper.update(patch, Wrappers.<ImMessageDO>lambdaUpdate()
            .eq(ImMessageDO::getReceiverId, me)
            .eq(ImMessageDO::getSenderId, peer)
            .eq(ImMessageDO::getIsRead, 0));
        if (affected > 0) {
            Map<String, Object> ack = new LinkedHashMap<>();
            ack.put("peerId", me);
            ack.put("count", affected);
            wsSessionManager.push(peer, WsPayload.of(WsPayload.Type.READ, ack));
        }
        return affected;
    }

    /**
     * 构造下行视图。注意 BaseModel 的时间字段带 @JsonIgnore，直接序列化 DO 会丢失时间，
     * 因此此处显式组装字段名。
     */
    private Map<String, Object> toView(ImMessageDO m) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", m.getId());
        view.put("senderId", m.getSenderId());
        view.put("receiverId", m.getReceiverId());
        view.put("content", m.getContent());
        view.put("isRead", m.getIsRead());
        view.put("createTime", formatTime(m.getCreateTime()));
        view.put("senderName", displayName(m.getSenderId()));
        return view;
    }

    private String displayName(Integer userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            return "未知用户";
        }
        String nickname = user.getNickname();
        return (nickname == null || nickname.isBlank()) ? user.getUsername() : nickname;
    }

    private static String formatTime(java.util.Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
