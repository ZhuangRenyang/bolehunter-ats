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
import com.bolehunter.mapper.ChatMapper;
import com.bolehunter.model.ChatDO;
import com.bolehunter.service.ChatService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @Param eval
 * @Time 2022/9/28 12:58
 */
@Service
public class ChatServiceImpl implements ChatService {
    @Resource
    ChatMapper chatMapper;

    @Resource
    RedisServiceImpl redisService;

    @Override
    public List<ChatDO> getChatById(Integer id) {
        QueryWrapper<ChatDO> wrapper = new QueryWrapper<>();
//        wrapper.
        wrapper.eq("send_id",id).or().eq("recv_id",id);
        return chatMapper.selectList(wrapper);
    }

    @Override
    public void insertChat(ChatDO chat) {
        chatMapper.insert(chat);
    }

    @Override
    public boolean getChatExistById(Integer selfID, Integer targetID) {
        QueryWrapper<ChatDO> wrapper = new QueryWrapper<>();
        wrapper.eq("send_id",selfID).eq("recv_id",targetID);
        return chatMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void changeChatState(String sendID, String recvID,boolean code) {
        String data = getChatState(recvID);
        if(!code && data.indexOf("^"+sendID+"&") ==-1){
            data += "^"+sendID+"&";
            redisService.set("chatState_"+recvID,data);
            return;
        }
        data = data.replace("^"+sendID+"&","");
        if(data.isEmpty()){
            redisService.del("chatState_"+recvID);
            return;
        }
        redisService.set("chatState_"+recvID,data);
    }

    /**
     * @param userID
     * @return
     */
    @Override
    public String getChatState(String userID) {
        String data = String.valueOf(redisService.get("chatState_"+userID));
        return data.equals("null") ? "" : data;
    }
}
